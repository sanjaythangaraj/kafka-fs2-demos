package simplekafkademo

import cats.effect.{IO, IOApp}
import fs2.*
import fs2.kafka.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import simplekafkademo.config.*
import simplekafkademo.model.*

import scala.concurrent.duration.*
import scala.language.postfixOps

object TemperatureAlertSystem extends IOApp.Simple {

  def processRecord(
      record: ConsumerRecord[String, String]
  ): IO[Option[ProducerRecord[String, String]]] = {
    val either: Either[Error, SensorData] = decode[SensorData](record.value)
    either match {
      case Right(data) =>
        if (data.temperature > 35) {
          val alert =
            Alert(data.sensor_id, data.timestamp, s"High temperature: ${data.temperature}")
          val alertRecord = ProducerRecord("alerts", record.key, alert.asJson.noSpaces)
          IO.pure(Some(alertRecord))
        } else {
          IO.pure(None)
        }
      case Left(_) =>
        val invalidRecord = ProducerRecord("invalid-messages", record.key, record.value)
        IO.pure(Some(invalidRecord))
    }
  }

  val stream: Stream[IO, Unit] =
    KafkaProducer.stream(producerSettings).flatMap { producer =>
      KafkaConsumer
        .stream(consumerSettings)
        .subscribeTo("sensor-data")
        .records
        .evalTap { committable =>
          IO.println(committable.record)
        }
        .evalMap { committable =>
          processRecord(committable.record).map { maybeRecord =>
            (committable.offset, maybeRecord)
          }
        }
        .collect { case (offset, Some(producerRecord)) => (offset, producerRecord) }
        .evalMap { case (offset, producerRecord) =>
          producer.produce(ProducerRecords.one(producerRecord)).flatten.as(offset)
        }
        .through(commitBatchWithin(500, 10 seconds))

    }

  override def run: IO[Unit] = stream.compile.drain
}
