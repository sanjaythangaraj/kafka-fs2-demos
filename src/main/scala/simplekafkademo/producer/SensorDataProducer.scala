package simplekafkademo.producer

import cats.effect.{IO, IOApp}
import fs2.*
import fs2.kafka.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import simplekafkademo.config.producerSettings
import simplekafkademo.model.SensorData

import java.time.Instant
import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.util.Random

object SensorDataProducer extends IOApp.Simple {

  def generateSensorData: IO[SensorData] = for {
    random    <- IO(Random.nextInt(10))
    timestamp <- IO(Instant.now().toString)
    temp      <- IO(Random.nextDouble() * 50)
  } yield SensorData(s"sensor$random", timestamp, temp)

  val producerStream: Stream[IO, ProducerRecords[String, String]] =
    Stream
      .awakeEvery[IO](1 second)
      .evalMap { _ =>
        generateSensorData.map { data =>
          val record =
            ProducerRecord[String, String]("sensor-data", data.sensor_id, data.asJson.noSpaces)
          ProducerRecords.one(record)
        }

      }

  val kafkaProducer: Stream[IO, Unit] = KafkaProducer
    .stream(producerSettings)
    .flatMap { producer =>
      val pipe: Pipe[IO, ProducerRecords[String, String], ProducerResult[String, String]] =
        KafkaProducer.pipe(producer)
      val result: Stream[IO, ProducerResult[String, String]] = producerStream.through(pipe)
      result.evalMap(IO.println)
    }

  override def run: IO[Unit] = kafkaProducer.compile.drain
}
