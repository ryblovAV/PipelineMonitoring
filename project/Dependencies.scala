import sbt._


object Dependencies {

  object Circe {
    private val version = "0.13.0"
    val libs = Seq(
      "io.circe" %% "circe-core" % version,
      "io.circe" %% "circe-generic" % version,
      "io.circe" %% "circe-parser" % version,
      "io.circe" %% "circe-generic-extras" % version,
      "io.circe" %% "circe-optics" % version
    )
  }

  object Doobie {
    private val version = "0.13.4"

    val libs = Seq(
      "org.tpolecat" %% "doobie-core" % version,
      "org.tpolecat" %% "doobie-postgres" % version,
      "org.tpolecat" %% "doobie-hikari" % version
    )
  }

  object Http4s {
    val version = "0.21.22"

    val libs = Seq(
      "org.http4s" %% "http4s-blaze-server" % version,
      "org.http4s" %% "http4s-circe" % version,
      "org.http4s" %% "http4s-dsl" % version
    )
  }

  object Fs2Kafka {
    val version = "1.8.0"

    val libs = Seq(
      "com.github.fd4s" %% "fs2-kafka" % version
    )
  }

  object Spark {
    val version = "3.2.0"

    val libs = Seq(
      "org.apache.spark" %% "spark-core" % version,
      "org.apache.spark" %% "spark-sql" % version
    )
  }

  object ScalaTest {
    val version = "3.2.10"

    val libs = Seq(
      "org.scalatest" %% "scalatest" % version % Test
    )
  }
}