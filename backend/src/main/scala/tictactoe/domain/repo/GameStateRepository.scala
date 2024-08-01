package tictactoe.domain.repo

import tictactoe.domain.model._
import zio._

trait GameStateRepository {
  def store(state: GameState): Task[Unit]

  def get(id: GameId): Task[Option[GameState]]

  def list: Task[List[GameState]]
}
