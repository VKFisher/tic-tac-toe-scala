package tictactoe.infra.http

import dev.profunktor.pulsar.*
import dev.profunktor.pulsar.schema.PulsarSchema
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.apache.pulsar.client.api.{MessageId, Schema}
import tictactoe.domain.model.*
import tictactoe.domain.model.Event.MoveAcceptedEvent
import tictactoe.domain.repo.GameStateRepository
import tictactoe.infra.pulsar.PulsarProducer
import tictactoe.infra.repo.InMemoryGameStateRepository
import zhttp.http.*
import zio.stream.ZStream
import zio.stream.interop.fs2z.*
import zio.{Config as *, *}

import java.util.UUID

object TicTacToeHttpApp {
  def layer: RLayer[GameStateRepository, TicTacToeHttpApp] =
    ZLayer.scoped {
      for {
        repo <- ZIO.service[GameStateRepository]
        producer <- PulsarProducer.make
      } yield TicTacToeHttpApp(
        repo,
        producer
      )
    }

  def default: TaskLayer[TicTacToeHttpApp] =
    InMemoryGameStateRepository.layer >>> layer

}

case class TicTacToeHttpApp(
    gameStateRepo: GameStateRepository,
    producer: Producer[Task, String]
) {
  private def processMoveCommand(move: Move, gameId: GameId): Task[MessageId] =
    ZIO.scoped {
      for {
        currentState <- gameStateRepo
          .get(gameId)
          .flatMap(ZIO.fromOption(_))
          .orElseFail(new Exception(s"Game $gameId not found"))
        event = moveToEvent(move, currentState)
        messageId <- producer
          .send(event.asJson.toString)
          .zipLeft(ZIO.logDebug(s"sent: $event"))
      } yield messageId
    }

  def apply(): Http[Any, Response, Request, Response] =
    Http.collectZIO[Request] {
      // get game listing
      case Method.GET -> _ / "tic-tac-toe" / "list" =>
        for {
          list <- gameStateRepo.list
            .orElseFail(Response.status(Status.InternalServerError))
          res = Response.json(list.asJson.toString)
        } yield res

      // start new game, returning the initial state
      case Method.POST -> _ / "tic-tac-toe" / "start" =>
        for {
          newId <- Random.nextUUID.map(GameId.apply)
          startTime <- Clock.instant
          newState = GameState.initial(newId, startTime)
          _ <- gameStateRepo
            .store(newState)
            .orElseFail(Response.status(Status.InternalServerError))
          res = Response.json(newState.asJson.toString)
        } yield res

      // get game state by id -> GameState
      // GET /tic-tac-toe/:gameId
      case Method.GET -> _ / "tic-tac-toe" / gameId =>
        for {
          id <- ZIO
            .attempt(UUID.fromString(gameId))
            .mapBoth(_ => Response.status(Status.BadRequest), GameId.apply)
          state <- gameStateRepo
            .get(id)
            .orElseFail(Response.status(Status.InternalServerError))
            .flatMap(x =>
              ZIO.fromOption(x).orElseFail(Response.status(Status.NotFound))
            )
          res = Response.json(state.asJson.toString)
        } yield res

      // make move (id, move(side, coords)) -> Either Error GameState
      case req @ Method.POST -> _ / "tic-tac-toe" / gameId / "move" =>
        for {
          id <- ZIO
            .attempt(UUID.fromString(gameId))
            .mapBoth(_ => Response.status(Status.BadRequest), GameId.apply)
          body <- req.bodyAsCharSequence.mapBoth(
            _ => Response.status(Status.InternalServerError),
            _.toString()
          )
          move <- ZIO
            .fromEither(decode[Move](body))
            .orElseFail(Response.status(Status.BadRequest))

          _ <- processMoveCommand(move, id)
            .orElseFail(Response.status(Status.InternalServerError))

        } yield Response.text("Ok")

    }
}
