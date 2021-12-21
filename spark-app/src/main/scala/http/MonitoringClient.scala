package http

import cats.effect.{ContextShift, IO, Timer}
import model.{DataSourceRead, SparkAppEnd, SparkAppFailed, SparkAppStart}
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._

import scala.concurrent.ExecutionContext
import org.http4s.implicits._


object MonitoringClient  {

  import scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityCodec._

  val uri = uri"http://localhost:9001"

  def sendDataSourceRead(appName: String, path: String): IO[String] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[String](Method.POST(DataSourceRead(appName, path), uri / "datasources" / "add" / "input"))
      }
  }

  def sendSparkAppStart(appName: String, outputs: List[String]): IO[Int] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[Int](Method.POST(SparkAppStart(appName, outputs), uri / "pipelines" / "start"))
      }
  }

  def sendSparkAppEnd(appName: String): IO[String] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[String](Method.POST(SparkAppEnd(appName), uri / "pipelines" / "end"))
      }
  }

  def sendSparkAppFailed(appName: String, error: Throwable): IO[String] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[String](Method.POST(SparkAppFailed(appName, error.getMessage), uri / "pipelines" / "failed"))
      }
  }
}
