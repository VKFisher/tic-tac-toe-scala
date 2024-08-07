package tictactoe.infra.http

import java.util.UUID

import dev.profunktor.pulsar._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.apache.pulsar.client.api.MessageId
import zio._
import zio.http._

import tictactoe.domain.model.Event.MoveAcceptedEvent
import tictactoe.domain.model._
import tictactoe.domain.repo.GameStateRepository
import tictactoe.infra.pulsar.PulsarProducer
import tictactoe.infra.repo.InMemoryGameStateRepository

object TicTacToeHttpApp {
  def layer: RLayer[GameStateRepository, TicTacToeHttpApp] =
    ZLayer.scoped {
      for {
        repo     <- ZIO.service[GameStateRepository]
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

  val routes: Routes[Any, Response] = Routes(
    // get game listing
    Method.GET / "tic-tac-toe" / "list" -> handler {
      for {
        list <- gameStateRepo.list
          .orElseFail(Response.status(Status.InternalServerError))
        res = Response.json(list.asJson.toString)
      } yield res
    },

    // start new game, returning the initial state
    Method.POST / "tic-tac-toe" / "start" -> handler {
      for {
        newId     <- Random.nextUUID.map(GameId.apply)
        startTime <- Clock.instant
        newState = GameState.initial(newId, startTime)
        _ <- gameStateRepo
          .store(newState)
          .orElseFail(Response.status(Status.InternalServerError))
        res = Response.json(newState.asJson.toString)
      } yield res
    },

    // get game state by id -> GameState
    // GET /tic-tac-toe/:gameId
    Method.GET / "tic-tac-toe" / string("gameId") -> handler { (gameId: String, _: Request) =>
      for {
        id <- ZIO
          .attempt(UUID.fromString(gameId))
          .mapBoth(_ => Response.status(Status.BadRequest), GameId.apply)
        state <- gameStateRepo
          .get(id)
          .orElseFail(Response.status(Status.InternalServerError))
          .flatMap(x => ZIO.fromOption(x).orElseFail(Response.status(Status.NotFound)))
        res = Response.json(state.asJson.toString)
      } yield res
    },

    // make move (id, move(side, coords)) -> Either Error GameState
    Method.POST / "tic-tac-toe" / string("gameId") / "move" -> handler { (gameId: String, req: Request) =>
      for {
        id <- ZIO
          .attempt(UUID.fromString(gameId))
          .mapBoth(_ => Response.status(Status.BadRequest), GameId.apply)
        body <- req.body.asString.mapBoth(
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
  )

}
