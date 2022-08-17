import simple_scala.game._

// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
class GameSuite extends munit.FunSuite {
  test("valid move") {
    val obtained: Option[GameField] = makeMove(
      Move(GameSide.O, Coordinates(row = 0, col = 1)),
      gameField.empty
    )
    val expected =
      Some(
        (None, Some(GameSide.O), None),
        (None, None, None),
        (None, None, None)
      )
    assertEquals(obtained, expected)
  }
  test("invalid move") {
    val obtained = for {
      step1 <- makeMove(
        Move(GameSide.O, Coordinates(row = 0, col = 0)),
        gameField.empty
      )
      step2 <- makeMove(
        Move(GameSide.O, Coordinates(row = 0, col = 0)),
        step1
      )
    } yield step2
    val expected = None
    assertEquals(obtained, expected)
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
    val expected = GameEnded(Win(O))
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
    val expected = GameEnded(Win(O))
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
    val expected = GameEnded(Win(X))
    assertEquals(obtained, expected)
  }
  test("ongoing game") {
    import GameSide._
    import GameStatus._
    import GameResult._
    val field = (
      (None, None, Some(X)),
      (Some(O), Some(X), Some(O)),
      (Some(O), None, Some(X))
    )
    val obtained: GameStatus = calculateStatus(field)
    val expected = GameOngoing
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
    val expected = GameEnded(Draw)
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
    val expected = GameEnded(Draw)
    assertEquals(obtained, expected)
  }
}
