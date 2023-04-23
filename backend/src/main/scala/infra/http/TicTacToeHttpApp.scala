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
    val placeholderMoves = List(
      Move(side = GameSide.X, coords = Coordinates(row = 1, col = 1)),
      Move(side = GameSide.O, coords = Coordinates(row = 0, col = 0)),
      Move(side = GameSide.X, coords = Coordinates(row = 2, col = 0)),
      Move(side = GameSide.O, coords = Coordinates(row = 0, col = 2))
    )

    Http.collectZIO[Request] {
      // start new game, returning the initial state
      case req @ Method.POST -> !! / "tic-tac-toe" / "start" =>
        for {
          newId <- Random.nextUUID.map(GameId(_))
          newState = GameState.initial(newId)
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
          move <- ZIO
            .fromEither(decode[Move](req.bodyAsCharSequence.toString))
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
