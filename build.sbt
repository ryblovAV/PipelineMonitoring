name := "PipelineMonitoring"

version := "1.0"

scalaVersion := "2.13.6"

val circeVersion = "0.13.0"
val http4sVersion = "0.21.22"
val doobieVersion = "0.13.4"
val scalaTestVersion = "3.2.10"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,

  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,

  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)