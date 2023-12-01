package tictactoe.infra

import dev.profunktor.pulsar.*
import dev.profunktor.pulsar.schema.PulsarSchema
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.apache.pulsar.client.api.{MessageId, Schema}
import tictactoe.domain.model.*
import tictactoe.domain.model.Event.MoveAcceptedEvent
import tictactoe.domain.repo.GameStateRepository
import tictactoe.infra.pulsar.{PulsarConsumer, PulsarProducer}
import tictactoe.infra.repo.InMemoryGameStateRepository
import zhttp.http.*
import zio.*
import zio.interop.catz.*
import zio.stream.ZStream
import zio.stream.interop.fs2z.*

object GameStateUpdater {
  def layer: RLayer[GameStateRepository, GameStateUpdater] =
    ZLayer.scoped {
      for {
        repo <- ZIO.service[GameStateRepository]
        eventStream <- PulsarConsumer.make.map(
          _.autoSubscribe
            .toZStream(1)
            .mapZIO(x =>
              ZIO
                .fromEither(decode[Event](x))
                .orElseFail(
                  new Exception(
                    s"could not parse input $x as Event"
                  )
                )
            )
        )
      } yield GameStateUpdater(
        repo,
        eventStream
      )
    }
}

case class GameStateUpdater(
    gameStateRepo: GameStateRepository,
    eventStream: ZStream[Any, Throwable, Event]
) {

  def run: Task[Unit] = {
    val processMoveAccepted = (event: MoveAcceptedEvent) =>
      for {
        currentState <- gameStateRepo
          .get(event.gameId)
          .flatMap(ZIO.fromOption(_))
          .orElseFail(new Exception(s"Game ${event.gameId} not found"))
        newState = updateStateOnValidMove(event, currentState)
        _ <- gameStateRepo.store(newState)
      } yield ()
    eventStream
      .tap(event => ZIO.logDebug(s"got event $event"))
      .tapError(e => ZIO.logError(e.toString))
      .tap {
        case event: MoveAcceptedEvent =>
          processMoveAccepted(event)
        case _ => ZIO.unit
      }
      .runDrain
  }

}
