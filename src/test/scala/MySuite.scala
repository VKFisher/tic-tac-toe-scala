import simple_scala.game._

// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
class GameSuite extends munit.FunSuite {
  test("valid move") {
    val obtained: Option[GameField] = makeMove(
      Move(GameSide.Nought, Coordinates(row = 0, col = 1)),
      gameField.empty
    )
    val expected =
      Some(
        (None, Some(GameSide.Nought), None),
        (None, None, None),
        (None, None, None)
      )
    assertEquals(obtained, expected)
  }
  test("invalid move") {
    val obtained = for {
      step1 <- makeMove(
        Move(GameSide.Nought, Coordinates(row = 0, col = 0)),
        gameField.empty
      )
      step2 <- makeMove(
        Move(GameSide.Nought, Coordinates(row = 0, col = 0)),
        step1
      )
    } yield step2
    val expected = None
    assertEquals(obtained, expected)
  }
}
