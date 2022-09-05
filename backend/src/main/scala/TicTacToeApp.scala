package simple_scala

import zhttp.http._
import simple_scala.game._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

/** An http app that:
  *   - Accepts a `Request` and returns a `Response`
  *   - Does not fail
  *   - Does not use the environment
  */
object TicTacToeApp {
  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {

      // get game state by id -> Option GameState
      // GET /tic-tac-toe/:gameId
      case req @ Method.GET -> !! / "tic-tac-toe" / gameId =>
        // placeholder
        movesToField(moves =
          List(
            Move(side = GameSide.X, coords = Coordinates(row = 1, col = 1)),
            Move(side = GameSide.O, coords = Coordinates(row = 0, col = 0)),
            Move(side = GameSide.X, coords = Coordinates(row = 2, col = 0)),
            Move(side = GameSide.O, coords = Coordinates(row = 0, col = 2))
          )
        ) match
          case Left(err) => Response.status(Status.BadRequest)
          case Right(x)  => Response.json(x.asJson.toString)

      // make move (id, move(side, coords)) -> Either Error GameState
      case req @ (Method.POST -> !! / "tic-tac-toe" / gameId / "move") =>
        val moveResult = for {
          move <- decode[Move](req.bodyAsCharSequence.toString)
          prevField <- movesToField(moves =
            List(
              Move(side = GameSide.X, coords = Coordinates(row = 1, col = 1)),
              Move(side = GameSide.O, coords = Coordinates(row = 0, col = 0)),
              Move(side = GameSide.X, coords = Coordinates(row = 2, col = 0)),
              Move(side = GameSide.O, coords = Coordinates(row = 0, col = 2))
            )
          )
          updatedField <- makeMove(move, prevField)
        } yield updatedField
        // TODO : change to GameState
        moveResult match
          case Left(err) => Response.status(Status.BadRequest)
          case Right(x)  => Response.json(x.asJson.toString)

      case req @ Method.GET -> !! / "tic-tac-toe" / gameId / "game-side" =>
        Response.json(GameSide.X.asJson.toString)
    }
}
