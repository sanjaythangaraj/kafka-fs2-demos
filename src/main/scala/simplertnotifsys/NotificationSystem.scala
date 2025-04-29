package simplertnotifsys

import cats.effect.*
import fs2.kafka.*
import fs2.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import scala.language.postfixOps
import scala.concurrent.duration.*

import simplertnotifsys.config.*
import simplertnotifsys.model.*

object NotificationSystem extends IOApp.Simple {

  def processSubscriptionRecords(
      record: ConsumerRecord[String, String],
      stateRef: Ref[IO, SubscriptionState]
  ): IO[Unit] = {

    val either: Either[Error, SubscriptionMessage] = decode[SubscriptionMessage](record.value)
    either match {
      case Right(SubscriptionMessage(userId, eventType, action)) =>
        stateRef.update { current =>
          val currentSubscriptions = current.map.getOrElse(userId, Set.empty)
          val updatedSubscriptions = action match {
            case "subscribe"   => currentSubscriptions + eventType
            case "unsubscribe" => currentSubscriptions - eventType
          }
          SubscriptionState(current.map.updated(userId, updatedSubscriptions))
        }
      case Left(_) => IO.unit
    }
  }

  def processEventRecords(
      record: ConsumerRecord[String, String],
      stateRef: Ref[IO, SubscriptionState]
  ): IO[Unit] =
    val either: Either[Error, EventMessage] = decode[EventMessage](record.value)
    either match {
      case Right(EventMessage(eventId, eventType, payload)) =>
        stateRef.get.flatMap { state =>
          val users = state.map.collect {
            case (userId, eventTypes) if eventTypes.contains(eventType) => userId
          }
          IO.parSequenceN(10)(users.map(userId => sendNotification(userId, payload)).toList).void
        }
      case Left(_) => IO.unit
    }

  def sendNotification(userId: String, event: String): IO[Unit] =
    IO.println(s"Sending notification to $userId: $event")

  val subscriptionStream: Ref[IO, SubscriptionState] => Stream[IO, Unit] = stateRef =>
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo("subscriptions")
      .records
      .evalTap { committable =>
        IO.println(committable.record.value)
      }
      .evalMap { committable =>
        processSubscriptionRecords(committable.record, stateRef).map(_ => committable.offset)
      }
      .through(commitBatchWithin(100, 10 seconds))

  val eventStream: Ref[IO, SubscriptionState] => Stream[IO, Unit] = stateRef =>
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo("events")
      .records
      .evalTap { committable =>
        IO.println(committable.record.value)
      }
      .evalMap { committable =>
        processEventRecords(committable.record, stateRef).map(_ => committable.offset)
      }
      .through(commitBatchWithin(100, 10 seconds))

  override def run: IO[Unit] = Stream
    .eval(Ref.of[IO, SubscriptionState](SubscriptionState(Map.empty)))
    .flatMap { stateRef =>
      subscriptionStream(stateRef).concurrently(eventStream(stateRef))
    }
    .compile
    .drain
}
