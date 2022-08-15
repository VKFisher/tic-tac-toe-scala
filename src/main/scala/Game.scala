package simple_scala.game

enum GameSide:
  case Nought
  case Cross

  def oppositeSide(x: GameSide): GameSide =
    x match
      case Nought => Cross
      case Cross  => Nought
end GameSide

type Index = 0 | 1 | 2

def getAt[A](i: Index, t: (A, A, A)): A =
  i match
    case 0 => t._1
    case 1 => t._2
    case 2 => t._3

def placeAt[A](i: Index, x: A, t: (A, A, A)): (A, A, A) =
  i match
    case 0 => t.copy(_1 = x)
    case 1 => t.copy(_2 = x)
    case 2 => t.copy(_3 = x)

def updateAt[A](i: Index, f: A => A, t: (A, A, A)): (A, A, A) =
  i match
    case 0 => t.copy(_1 = f(t._1))
    case 1 => t.copy(_2 = f(t._2))
    case 2 => t.copy(_3 = f(t._3))

case class Coordinates(row: Index, col: Index)

case class Move(side: GameSide, coords: Coordinates)

type FieldState = Option[GameSide]

type GameField = (
    (FieldState, FieldState, FieldState),
    (FieldState, FieldState, FieldState),
    (FieldState, FieldState, FieldState)
)

object gameField:
  val empty: GameField = (
    (None, None, None),
    (None, None, None),
    (None, None, None)
  )

// TODO: refactor to get rid of Option
def calculateGameField(moves: List[Move]): Option[GameField] =
  Some(gameField.empty)

enum MoveRejectionReason:
  case NotYourTurn
  case FieldOccupied
  case GameEnded

enum GameResult:
  case Draw
  case Win(winningSide: GameSide)

enum GameStatus:
  case GameOngoing
  case GameEnded(result: GameResult)

def assessGameStatus(moves: List[Move]): GameStatus =
  if (true) GameStatus.GameOngoing
  else GameStatus.GameEnded(GameResult.Draw)

type Moves = List[Move]
case class InferredGameState(status: GameStatus, field: GameField)

// f: Moves -> InferredGameState

// TODO: переделать на Move, GameState -> Either[MoveRejectionReason, GameState]
def makeMove(move: Move, gf: GameField): Option[GameField] =
  val fs: FieldState =
    getAt(move.coords.col, getAt(move.coords.row, gf))
  val newFs: FieldState = Some(move.side)
  fs match
    case Some(occupied) => None
    case None =>
      Some(updateAt(move.coords.row, placeAt(move.coords.col, newFs, _), gf))
