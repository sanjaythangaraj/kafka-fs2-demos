ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-fs2-demos"
  )

libraryDependencies ++= Seq(
  "com.github.fd4s" %% "fs2-kafka" % "3.7.0",
  "io.circe" %% "circe-core" % "0.14.13",
  "io.circe" %% "circe-parser" % "0.14.13",
  "io.circe" %% "circe-generic" % "0.14.13"
)
