
name := "PipelineMonitoring"

version := "1.0"

ThisBuild / scalaVersion := "2.13.6"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
)

val circeVersion = "0.13.0"
val http4sVersion = "0.21.22"
val doobieVersion = "0.13.4"
val scalaTestVersion = "3.2.10"
val scalaJSVersion = "2.0.0"

val laminarVersion       = "0.14.2"
val laminextVersion      = "0.14.2"

val sparkVersion = "3.2.0"

val fs2KafkaVersion = "1.8.0"

val sharedSettings = Seq(
  scalafmtOnCompile := true,
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
  scalacOptions ++= Seq("-Ymacro-annotations", "-Xfatal-warnings", "-deprecation")
)

lazy val common =
  project
    .in(file("common"))
    .settings(
      name := "Common",
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic-extras" % circeVersion,
        "io.circe" %% "circe-optics" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion
      )
    )
    .settings(sharedSettings)

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

        "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion,

        "org.scalatest" %% "scalatest" % scalaTestVersion % Test
      )
    )
    .settings(sharedSettings)
    .dependsOn(common)

lazy val frontend =
  project
    .in(file("./frontend"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "WebApp",
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++= Seq(
        "com.raquo" %%% "laminar" % laminarVersion,

        "io.laminext" %%% "core" % laminextVersion,
        "io.laminext" %%% "websocket" % laminextVersion,
        "io.laminext" %%% "websocket-circe" % laminextVersion,

        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-generic-extras" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion
      ),
    )
    .settings(sharedSettings)
    .dependsOn(common)

lazy val sparkApp =   project
  .in(file("./spark-app"))
  .settings(
    name := "SparkApp",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,

      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,

      "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion,

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "io.circe" %% "circe-optics" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "org.scalatest" %% "scalatest" % "3.2.10" % Test
    ),
    resolvers += "confluent" at "https://packages.confluent.io/maven/"
  )
  .settings(sharedSettings)
  .dependsOn(common)