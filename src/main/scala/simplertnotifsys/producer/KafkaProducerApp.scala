package simplertnotifsys.producer

import cats.effect.*
import fs2.*
import fs2.kafka.*
import io.circe.generic.auto.*
import io.circe.syntax.*


import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.util.Random

import simplertnotifsys.config.*
import simplertnotifsys.model.*

object KafkaProducerApp extends IOApp.Simple {

  // Stream to generate subscription messages every 10 seconds
  val subscriptionStream: Stream[IO, ProducerRecords[String, String]] =

    def generateSubscriptionMessage: IO[SubscriptionMessage] = for {
      userId    <- IO(s"user${Random.nextInt(5)}")
      eventType <- IO(s"type${Random.nextInt(3)}")
      action <- IO {
        if (Random.nextBoolean()) "subscribe"
        else "unsubscribe"
      }
    } yield SubscriptionMessage(userId, eventType, action)

    Stream
      .awakeEvery[IO](10 seconds)
      .evalMap(_ => generateSubscriptionMessage)
      .map { data =>
        val message = data.asJson.noSpaces
        ProducerRecords.one(ProducerRecord("subscriptions", data.userId, message))
      }

  // Stream to generate event messages every 5 seconds
  val eventStream: Stream[IO, ProducerRecords[String, String]] =

    def generateEventMessage: IO[EventMessage] = for {
      eventId <- IO(s"event${Random.nextInt(1000)}")
      eventType <- IO(s"type${Random.nextInt(3)}")
      payload <- IO(s"Event Payload ${Random.nextInt(100)}")
    } yield EventMessage(eventId, eventType, payload)

    Stream.awakeEvery[IO](5 seconds)
      .evalMap(_ => generateEventMessage)
      .map { data =>
        val message = data.asJson.noSpaces
        ProducerRecords.one(ProducerRecord("events", data.eventId, message))
      }

  val mergedStream: Stream[IO, ProducerRecords[String, String]] = subscriptionStream merge eventStream

  val kafkaProducer: Stream[IO, ProducerResult[String, String]] = KafkaProducer
    .stream(producerSettings)
    .flatMap { producer =>
      mergedStream.evalTap { records =>
        records.head match {
          case Some(record) => IO.println(record.value)
          case None => IO.unit
        }
      }.through { KafkaProducer.pipe(producer) }
    }

  override def run: IO[Unit] = kafkaProducer.compile.drain
}
