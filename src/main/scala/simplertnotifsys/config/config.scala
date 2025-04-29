package simplertnotifsys.config

import cats.effect.IO
import fs2.kafka.*

val serverConfig: String = "localhost:9092"
val consumerGroupId: String = "notification-system"

val producerSettings: ProducerSettings[IO, String, String] = ProducerSettings[IO, String, String]
  .withBootstrapServers(serverConfig)

val consumerSettings =
  ConsumerSettings[IO, String, String]
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withBootstrapServers(serverConfig)
    .withGroupId(consumerGroupId)
