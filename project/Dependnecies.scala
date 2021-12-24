import sbt._

object Dependencies {

  object Circe {
    private val version = "0.14.1"
    val libs = Seq(
      "io.circe" %% "circe-core" % version,
      "io.circe" %% "circe-generic" % version,
      "io.circe" %% "circe-parser" % version
    )
  }
}
