import Dependencies._

name := "PipelineMonitoring"

version := "1.0"

ThisBuild / scalaVersion := "2.13.6"

val laminarVersion = "0.14.2"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
)

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
      libraryDependencies ++= Circe.libs
    )
    .settings(sharedSettings)

lazy val backend =
  project
    .in(file("./backend"))
    .settings(
      name := "HttpApp",
      libraryDependencies ++= Circe.libs ++ Doobie.libs ++ Http4s.libs ++ Fs2Kafka.libs ++ ScalaTest.libs
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
      libraryDependencies ++=
        Seq(
          "com.raquo" %%% "laminar" % laminarVersion,

          "io.laminext" %%% "core" % laminarVersion,
          "io.laminext" %%% "websocket" % laminarVersion,
          "io.laminext" %%% "websocket-circe" % laminarVersion
        ) ++ Circe.libs
    )
    .settings(sharedSettings)
    .dependsOn(common)

lazy val sparkApp =   project
  .in(file("./spark-app"))
  .settings(
    name := "SparkApp",
    libraryDependencies ++= Circe.libs ++ Fs2Kafka.libs ++ Spark.libs ++ ScalaTest.libs,
    resolvers += "confluent" at "https://packages.confluent.io/maven/"
  )
  .settings(sharedSettings)
  .dependsOn(common)