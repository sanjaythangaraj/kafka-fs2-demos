package simplekafkademo.config

import cats.effect.IO
import fs2.kafka.*

val serverConfig: String = "localhost:9092"
val consumerGroupId: String = "temperature-alert-group"

val producerSettings: ProducerSettings[IO, String, String] = ProducerSettings[IO, String, String]
  .withBootstrapServers(serverConfig)

val consumerSettings =
  ConsumerSettings[IO, String, String]
    .withAutoOffsetReset(AutoOffsetReset.Latest)
    .withBootstrapServers(serverConfig)
    .withGroupId(consumerGroupId)
