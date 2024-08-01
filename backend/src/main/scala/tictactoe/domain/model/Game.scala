package tictactoe.domain.model

import java.time.Instant

import cats.implicits._
import cats.kernel.Eq
import io.circe._

import tictactoe.domain.model.Event._

enum GameSide:
  case O
  case X

end GameSide

implicit val eqGameSide: Eq[GameSide] = Eq.fromUniversalEquals

type Index = 0 | 1 | 2

implicit val encodeIndex: Encoder[Index] = (a: Index) => Json.fromInt(a)

implicit val decodeIndex: Decoder[Index] = Decoder.decodeInt.emap {
  case 0 => Right(0)
  case 1 => Right(1)
  case 2 => Right(2)
  case _ => Left("fuck it")
}

enum MoveRejectionReason:
  case NotYourTurn
  case FieldOccupied
  case GameEnded

enum GameResult:
  case Draw
  case Win(winningSide: GameSide)

enum GameStatus:
  case GameOngoing(nextMoveSide: GameSide)
  case GameEnded(result: GameResult)

case class GameState(
    id: GameId,
    startedAt: Instant,
    status: GameStatus,
    field: GameField,
    moves: List[Move]
)

object GameState:

  import GameSide.*
  import GameStatus.*

  def initial(id: GameId, startedAt: Instant): GameState =
    GameState(
      id = id,
      startedAt = startedAt,
      field = GameField.empty,
      status = GameOngoing(X),
      moves = List()
    )

def getAt[A](i: Index, t: (A, A, A)): A =
  i match
    case 0 => t._1
    case 1 => t._2
    case 2 => t._3

def placeAt[A](i: Index, x: A)(t: (A, A, A)): (A, A, A) =
  i match
    case 0 => t.copy(_1 = x)
    case 1 => t.copy(_2 = x)
    case 2 => t.copy(_3 = x)

def updateAt[A](i: Index, f: A => A)(t: (A, A, A)): (A, A, A) =
  i match
    case 0 => t.copy(_1 = f(t._1))
    case 1 => t.copy(_2 = f(t._2))
    case 2 => t.copy(_3 = f(t._3))

def tupleToList[A](t: (A, A, A)): List[A] = List(t._1, t._2, t._3)

case class Coordinates(row: Index, col: Index)

case class Move(side: GameSide, coords: Coordinates)

type CellState = Option[GameSide]

/** Note: Line – не то же самое, что Row. Line – это три последовательных cell по горизонтали, вертикали или диагонали
  * Возможно, мы захотим сделать Line case-классом, чтобы сохранять направление
  */
type Line = (CellState, CellState, CellState)

def lineWinner(line: Line): Option[GameSide] =
  import GameSide.*
  line match
    case (Some(O), Some(O), Some(O)) => Some(O)
    case (Some(X), Some(X), Some(X)) => Some(X)
    case _                           => None

def hasWinPotential(line: Line): Boolean =
  val (a, b, c) = line
  List(a, b, c).flatten.distinct.length < 2

type GameField = (
    (CellState, CellState, CellState),
    (CellState, CellState, CellState),
    (CellState, CellState, CellState)
)

object GameField:
  val empty: GameField = (
    (None, None, None),
    (None, None, None),
    (None, None, None)
  )

  def lines(gf: GameField): List[Line] =
    val (
      (a0, b0, c0),
      (a1, b1, c1),
      (a2, b2, c2)
    ) = gf

    List(
      // horizontal
      (a0, b0, c0),
      (a1, b1, c1),
      (a2, b2, c2),

      // vertical
      (a0, a1, a2),
      (b0, b1, b2),
      (c0, c1, c2),

      // diagonal
      (a0, b1, c2),
      (a2, b1, c0)
    )

  def cellList(gf: GameField): List[CellState] =
    tupleToList(gf).flatMap(tupleToList)

def fieldWinner(gf: GameField): Option[GameSide] =
  GameField.lines(gf).map(lineWinner).find(_.isDefined).flatten

def isDraw(gf: GameField): Boolean =
  !GameField.lines(gf).exists(hasWinPotential)

def calculateStatus(gf: GameField): GameStatus =
  import GameResult.*
  import GameStatus.*
  fieldWinner(gf) match
    case Some(winner) => GameEnded(Win(winner))
    case None =>
      if isDraw(gf) then GameEnded(Draw) else GameOngoing(nextMoveSide(gf))

def nextMoveSide(gf: GameField): GameSide =
  val cells = GameField.cellList(gf).flatten
  if cells.length % 2 === 0 then GameSide.X else GameSide.O

def moveToEvent(
    move: Move,
    gs: GameState
): Event = {
  val validateMove: Either[MoveRejectionReason, Unit] = for {
    nextSide <- calculateStatus(gs.field) match
      case GameStatus.GameEnded(_)          => Left(MoveRejectionReason.GameEnded)
      case GameStatus.GameOngoing(nextSide) => Right(nextSide)
    _ <-
      if nextSide === move.side then Right(())
      else Left(MoveRejectionReason.NotYourTurn)
    cellState = getAt(move.coords.col, getAt(move.coords.row, gs.field))
    _ <- cellState match
      case None    => Right(())
      case Some(_) => Left(MoveRejectionReason.FieldOccupied)
  } yield ()

  validateMove.fold(
    rejectionReason =>
      Event.MoveRejectedEvent(
        gameId = gs.id,
        move = move,
        rejectionReason = rejectionReason
      ),
    _ =>
      Event.MoveAcceptedEvent(
        gameId = gs.id,
        move = move
      )
  )
}

def updateStateOnMove(
    event: Event,
    gs: GameState
): GameState = {
  event match {
    case MoveAcceptedEvent(gameId, move) => {
      val newField = updateAt(
        move.coords.row,
        placeAt(move.coords.col, move.side.some)(_)
      )(gs.field)
      val newStatus = calculateStatus(newField)
      GameState(
        id = gs.id,
        startedAt = gs.startedAt,
        field = newField,
        status = newStatus,
        moves = move :: gs.moves
      )
    }
    case MoveRejectedEvent(gameId, move, rejectionReason) => gs
  }

}
