package http

import cats.effect.{ContextShift, IO, Timer}
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import scala.concurrent.ExecutionContext
import org.http4s.implicits._

case class DataSourceAttr(pipelineJobId: Int, dataSourcePath: String)

object HttpClient  {

  import scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityCodec._

  val uri = uri"http://localhost:9001"

  def send(dataSourceAttr: DataSourceAttr): IO[String] = {

    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[String](Method.POST(dataSourceAttr, uri / "datasources" / "add" / "output"))
      }
  }
}
