package monitoring.routes

import cats.effect.Concurrent
import cats.syntax.all._
import model.{PipelineStatusEvent, SparkAppStart}
import fs2.concurrent.Topic
import monitoring.domain.Codecs._
import monitoring.services.Pipelines
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{Header, HttpRoutes}



final case class PipelineRoutes[F[_]: Concurrent](pipelines: Pipelines[F]) extends Http4sDsl[F] {
  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityCodec._
  import io.circe.syntax._

  private val prefixPath = "/pipelines"

  private def httpRoutes(eventTopic: Topic[F, Option[PipelineStatusEvent]]): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "start"  =>
        for {
          attr <- req.as[SparkAppStart]
          pipelineJobId <- pipelines.startApp(attr)
          _ <- eventTopic.publish1(Some(PipelineStatusEvent(attr.pipelineName, "running")))
          response <- Ok(pipelineJobId)
        } yield response
      case req @ POST -> Root / "end"  =>
        // TODO implement logic + send event
        Ok("1")
      case req @ POST -> Root / "failed" =>
        // TODO implement logic + send event
        Ok("1")
      case GET -> Root =>
        Ok(pipelines.getAllJobs)
          .map(response =>
            response.copy(headers = response.headers.put(Header("Access-Control-Allow-Origin", "*")))
          )
      case GET -> Root / "info" =>
        WebSocketBuilder[F].build(
          receive = _.map(_ => ()),
          send = eventTopic
            .subscribe(maxQueued = 1000)
            .collect { case Some(event) => WebSocketFrame.Text(event.asJson.toString()) }
        )
    }
  }

  def routes(eventTopic: Topic[F, Option[PipelineStatusEvent]]): HttpRoutes[F] = {
    Router(
      prefixPath -> httpRoutes(eventTopic)
    )
  }
}