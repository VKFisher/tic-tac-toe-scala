package tictactoe.infra.repo

import zio._

import tictactoe.domain.model._
import tictactoe.domain.repo.GameStateRepository

object InMemoryGameStateRepository {
  private def init: Task[GameStateRepository] =
    for {
      states <- Ref.make[Map[GameId, GameState]](Map.empty)
    } yield InMemoryGameStateRepository(states)

  def layer: TaskLayer[GameStateRepository] = ZLayer(init)
}

case class InMemoryGameStateRepository(states: Ref[Map[GameId, GameState]]) extends GameStateRepository {

  def store(state: GameState): Task[Unit] =
    states.update(_ + (state.id -> state))

  def get(id: GameId): Task[Option[GameState]] =
    states.get.map(_.get(id))

  def list: Task[List[GameState]] =
    states.get.map(_.values.toList)

}
