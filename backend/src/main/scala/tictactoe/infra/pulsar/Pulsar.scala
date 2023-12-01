package tictactoe.infra.pulsar

import cats.effect.Resource
import dev.profunktor.pulsar.*
import dev.profunktor.pulsar.schema.PulsarSchema
import org.apache.pulsar.client.api.Schema
import zio.interop.catz.*
import zio.{RIO, Scope, Task, Config as *}

object PulsarCommon {
  val config: Config = Config.Builder.default

  val topic: Topic.Single =
    Topic.Builder
      .withName("moves")
      .withConfig(config)
      .withType(Topic.Type.NonPersistent)
      .build

  val schema: Schema[String] = PulsarSchema.utf8

}

object PulsarProducer {
  val make: RIO[Scope, Producer[Task, String]] =
    for {
      pulsar <- Pulsar.make[Task](url = PulsarCommon.config.url).toScopedZIO
      producer <- Producer
        .make[Task, String](
          pulsar,
          PulsarCommon.topic,
          PulsarCommon.schema
        )
        .toScopedZIO
    } yield producer
}

object PulsarConsumer {

  private val subs: Subscription =
    Subscription.Builder
      .withName("moves-sub")
      .withType(Subscription.Type.Exclusive)
      .build

  val make: RIO[Scope, Consumer[Task, String]] =
    for {
      pulsar <- Pulsar.make[Task](url = PulsarCommon.config.url).toScopedZIO
      consumer <- Consumer
        .make[Task, String](
          pulsar,
          PulsarCommon.topic,
          subs,
          PulsarCommon.schema
        )
        .toScopedZIO
    } yield consumer

}
