package tictactoe.domain.repo

import zio._

import tictactoe.domain.model._

trait GameStateRepository {
  def store(state: GameState): Task[Unit]

  def get(id: GameId): Task[Option[GameState]]

  def list: Task[List[GameState]]
}
