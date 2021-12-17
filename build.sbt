
name := "PipelineMonitoring"

version := "1.0"

ThisBuild / scalaVersion := "2.13.6"

val circeVersion = "0.13.0"
val http4sVersion = "0.21.22"
val doobieVersion = "0.13.4"
val scalaTestVersion = "3.2.10"
val scalaJSVersion = "2.0.0"

lazy val backend =
  project
    .in(file("./backend"))
    .settings(
      name := "HttpApp",
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
  )

lazy val frontend =
  project
    .in(file("./frontend"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "WebApp",
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % scalaJSVersion
      ),

    )