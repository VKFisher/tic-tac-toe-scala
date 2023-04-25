package tictactoe.infra.http

import zhttp.http.*
import tictactoe.domain.model.*
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import zio.*
import tictactoe.domain.repo.GameStateRepository
import java.util.UUID
import infra.repo.InMemoryGameStateRepository

object TicTacToeHttpApp {
  def layer = ZLayer.fromFunction(TicTacToeHttpApp(_))

  def default: TaskLayer[TicTacToeHttpApp] =
    InMemoryGameStateRepository.layer >>> layer
}

case class TicTacToeHttpApp(
    gameStateRepo: GameStateRepository
) {
  def apply(): Http[Any, Response, Request, Response] =
    Http.collectZIO[Request] {
      // get game listing
      case req @ Method.GET -> !! / "tic-tac-toe" / "list" =>
        for {
          list <- gameStateRepo.list
            .orElseFail(Response.status(Status.InternalServerError))
          res = Response.json(list.asJson.toString)
        } yield res

      // start new game, returning the initial state
      case req @ Method.POST -> !! / "tic-tac-toe" / "start" =>
        for {
          newId <- Random.nextUUID.map(GameId(_))
          startTime <- Clock.instant
          newState = GameState.initial(newId, startTime)
          _ <- gameStateRepo
            .store(newState)
            .orElseFail(Response.status(Status.InternalServerError))
          res = Response.json(newState.asJson.toString)
        } yield res

      // get game state by id -> GameState
      // GET /tic-tac-toe/:gameId
      case req @ Method.GET -> !! / "tic-tac-toe" / gameId =>
        for {
          id <- ZIO
            .attempt(UUID.fromString(gameId))
            .map(GameId(_))
            .mapError(_ => Response.status(Status.BadRequest))
          state <- gameStateRepo
            .get(id)
            .mapError(_ => Response.status(Status.InternalServerError))
            .flatMap(x =>
              ZIO.fromOption(x).orElseFail(Response.status(Status.NotFound))
            )
          res = Response.json(state.asJson.toString)
        } yield res

      // make move (id, move(side, coords)) -> Either Error GameState
      case req @ (Method.POST -> !! / "tic-tac-toe" / gameId / "move") =>
        for {
          id <- ZIO
            .attempt(UUID.fromString(gameId))
            .map(GameId(_))
            .mapError(_ => Response.status(Status.BadRequest))
          currentState <- gameStateRepo
            .get(id)
            .mapError(_ => Response.status(Status.InternalServerError))
            .flatMap(x =>
              ZIO.fromOption(x).orElseFail(Response.status(Status.NotFound))
            )
          body <- req.bodyAsCharSequence
            .map(_.toString())
            .mapError(_ => Response.status(Status.InternalServerError))
          move <- ZIO
            .fromEither(decode[Move](body))
            .mapError(_ => Response.status(Status.BadRequest))
          updatedState <- ZIO
            .fromEither(makeMove(move, currentState))
            .mapError(_ => Response.status(Status.BadRequest))
          _ <- gameStateRepo
            .store(updatedState)
            .orElseFail(Response.status(Status.InternalServerError))
          res = Response.json(updatedState.asJson.toString)
        } yield res

    }
}
