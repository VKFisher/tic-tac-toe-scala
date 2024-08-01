package tictactoe

import cats.effect.Resource
import dev.profunktor.pulsar.Config
import dev.profunktor.pulsar._
import dev.profunktor.pulsar.schema.PulsarSchema
import fs2.Stream
import zio.ZIOAppDefault
import zio.interop.catz._
import zio.stream.ZSink
import zio.stream.ZStream
import zio.stream.interop.fs2z._
import zio.{Config => _, _}

import tictactoe.infra.logging.Logging.devLoggingSetup

object PulsarDemo {

  val config: Config = Config.Builder.default

  private val topic =
    Topic.Builder
      .withName("my-topic")
      .withConfig(config)
      .withType(Topic.Type.NonPersistent)
      .build

  private val subs =
    Subscription.Builder
      .withName("my-sub")
      .withType(Subscription.Type.Exclusive)
      .build

  private val schema = PulsarSchema.utf8

  private val resources: Resource[Task, (Consumer[Task, String], Producer[Task, String])] =
    for {
      pulsar   <- Pulsar.make[Task](url = config.url)
      consumer <- Consumer.make[Task, String](pulsar, topic, subs, schema)
      producer <- Producer.make[Task, String](pulsar, topic, schema)
    } yield consumer -> producer

  val stream: ZStream[Any, Throwable, Unit] =
    Stream
      .resource(resources)
      .flatMap { case (consumer, producer) =>
        val consume =
          consumer.autoSubscribe
            .evalMap(x => ZIO.logInfo(s"consumed: $x"))

        val produce =
          Stream
            .emit("test data")
            .covary[Task]
            .metered({
              import scala.concurrent.duration._
              3.seconds
            })
            .evalMap(producer.send_)
            .evalMap(x => ZIO.logInfo(s"produced: $x"))

        consume.concurrently(produce)
      }
      .toZStream(1)

}

/** Used to run experiments during the development process
  *
  * sbt experiment
  */
object Experiment extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    devLoggingSetup(
      logLevel = LogLevel.Debug,
      removeDefaultLoggers = true,
      logToFile = None
    )

  private val layer = ZLayer.empty

  private val experiment = for {
    _ <- ZIO.logInfo("Start")
    _ <- PulsarDemo.stream.run(ZSink.drain)
  } yield ()

  def run: URIO[ZIOAppArgs, Unit] = {
    for {
      _         <- ZIO.logInfo("=== Experiment start ===")
      startedAt <- Clock.instant
      _         <- experiment
      endedAt   <- Clock.instant
      _         <- ZIO.logInfo(s"Execution time: ${Duration.fromInterval(startedAt, endedAt)}")
      _         <- ZIO.logInfo("=== Experiment end ===")
    } yield ()
  }
    .provideLayer(layer)
    .tapErrorCause(c => ZIO.logErrorCause("Critical error", c))
    .exitCode
    .flatMap(exit(_))

}
