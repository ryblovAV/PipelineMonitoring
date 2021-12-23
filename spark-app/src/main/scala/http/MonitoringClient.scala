package http

import cats.effect.{ContextShift, IO, Timer}
import model.Events.{DataSourceEvent, DataSourceEventType, PipelineFailed, PipelineFinish, PipelineStart}
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext

import model.EventCodecs._

object MonitoringClient  {

  import scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityCodec._

  // TODO use config
  val uri = uri"http://localhost:9001"

  def sendSparkAppStart(pipelineName: String): IO[Int] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[Int](Method.POST(PipelineStart(pipelineName), uri / "pipelines" / "start"))
      }
  }

  def sendSparkAppEnd(pipelineId: Int): IO[Int] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[Int](Method.POST(PipelineFinish(pipelineId), uri / "pipelines" / "finish"))
      }
  }

  def sendSparkAppFailed(pipelineId: Int, error: Throwable): IO[Int] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[Int](Method.POST(PipelineFailed(pipelineId, error.getMessage), uri / "pipelines" / "failed"))
      }
  }

  def sendDataSourceRead(pipelineId: Int, path: String): IO[Int] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[Int](Method.POST(DataSourceEvent(pipelineId, path, DataSourceEventType.read), uri / "datasources" / "read"))
      }
  }

  def sendDataSourceWrite(pipelineId: Int, path: String): IO[Int] = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        client.expect[Int](Method.POST(DataSourceEvent(pipelineId, path, DataSourceEventType.write), uri / "datasources" / "write"))
      }
  }
}
