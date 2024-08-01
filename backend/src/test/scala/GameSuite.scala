import java.time.Instant
import java.util.UUID

import tictactoe.domain.model._

class GameSuite extends munit.FunSuite {
  test("valid move turns to accepted event") {
    val gameId = GameId(UUID.fromString("00a11373-9ee5-4509-97fc-f33f7ee674ee"))
    val initialGameState = GameState.initial(
      id = gameId,
      startedAt = Instant.ofEpochSecond(12345)
    )
    val move = Move(GameSide.X, Coordinates(row = 0, col = 1))
    val expectedEvent = Event.MoveAcceptedEvent(
      gameId = gameId,
      move = move
    )
    val resultingEvent = moveToEvent(move, initialGameState)
    assertEquals(resultingEvent, expectedEvent)
  }

  test("accepted event is processed") {
    val gameId = GameId(UUID.fromString("00a11373-9ee5-4509-97fc-f33f7ee674ee"))
    val initialGameState = GameState.initial(
      id = gameId,
      startedAt = Instant.ofEpochSecond(12345)
    )
    val move = Move(GameSide.X, Coordinates(row = 0, col = 1))
    val event = Event.MoveAcceptedEvent(
      gameId = gameId,
      move = move
    )
    val expectedState = GameState(
      id = initialGameState.id,
      startedAt = initialGameState.startedAt,
      field = (
        (None, Option(GameSide.X), None),
        (None, None, None),
        (None, None, None)
      ),
      status = GameStatus.GameOngoing(GameSide.O),
      moves = List(move)
    )
    val resultingState = updateStateOnMove(event, initialGameState)
    assertEquals(resultingState, expectedState)
  }

  test("invalid move turns to rejected event") {
    val gameId = GameId(UUID.fromString("00a11373-9ee5-4509-97fc-f33f7ee674ee"))
    val initialGameState = GameState.initial(
      id = gameId,
      startedAt = Instant.ofEpochSecond(12345)
    )
    val move = Move(GameSide.O, Coordinates(row = 0, col = 1))
    val expectedEvent = Event.MoveRejectedEvent(
      gameId = gameId,
      move = move,
      rejectionReason = MoveRejectionReason.NotYourTurn
    )
    val resultingEvent = moveToEvent(move, initialGameState)
    assertEquals(resultingEvent, expectedEvent)
  }

  test("O wins vertical") {
    import GameSide._
    import GameStatus._
    import GameResult._
    val field = (
      (None, Some(O), Some(X)),
      (Some(X), Some(O), None),
      (Some(X), Some(O), None)
    )
    val obtained: GameStatus = calculateStatus(field)
    val expected             = GameEnded(Win(O))
    assertEquals(obtained, expected)
  }

  test("O wins horizontal") {
    import GameSide._
    import GameStatus._
    import GameResult._
    val field = (
      (Some(X), None, None),
      (Some(O), Some(O), Some(O)),
      (None, Some(X), Some(X))
    )
    val obtained: GameStatus = calculateStatus(field)
    val expected             = GameEnded(Win(O))
    assertEquals(obtained, expected)
  }

  test("X wins diagonal") {
    import GameSide._
    import GameStatus._
    import GameResult._
    val field = (
      (None, None, Some(X)),
      (Some(O), Some(X), Some(O)),
      (Some(X), None, None)
    )
    val obtained: GameStatus = calculateStatus(field)
    val expected             = GameEnded(Win(X))
    assertEquals(obtained, expected)
  }

  test("ongoing game") {
    import GameSide._
    import GameStatus._
    val field = (
      (None, None, Some(X)),
      (Some(O), Some(X), Some(O)),
      (Some(O), None, Some(X))
    )
    val obtained: GameStatus = calculateStatus(field)
    val expected             = GameOngoing(X)
    assertEquals(obtained, expected)
  }

  test("draw") {
    import GameSide._
    import GameStatus._
    import GameResult._
    val field = (
      (Some(X), Some(O), Some(X)),
      (Some(O), Some(O), Some(X)),
      (Some(X), Some(X), Some(O))
    )
    val obtained: GameStatus = calculateStatus(field)
    val expected             = GameEnded(Draw)
    assertEquals(obtained, expected)
  }

  test("draw with empty cells") {
    import GameSide._
    import GameStatus._
    import GameResult._
    val field = (
      (Some(X), Some(O), Some(X)),
      (Some(X), Some(O), Some(O)),
      (Some(O), Some(X), None)
    )
    val obtained: GameStatus = calculateStatus(field)
    val expected             = GameEnded(Draw)
    assertEquals(obtained, expected)
  }

}
