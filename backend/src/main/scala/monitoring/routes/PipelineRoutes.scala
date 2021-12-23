package monitoring.routes

import cats.effect.Concurrent
import cats.syntax.all._
import fs2.concurrent.Topic
import model.Events.{PipelineFailed, PipelineFinish, PipelineStart}
import model.PipelineInfo
import monitoring.services.Pipelines
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame


final case class PipelineRoutes[F[_]: Concurrent](pipelines: Pipelines[F]) extends Http4sDsl[F] {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import org.http4s.circe.CirceEntityCodec._

  private val prefixPath = "/pipelines"

  // TODO implement the calculation of pipelines states
  private def httpRoutes(eventTopic: Topic[F, List[PipelineInfo]]): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "start"  =>
        for {
          event <- req.as[PipelineStart]
          pipelineId <- pipelines.pipelineStart(event)
          _ <- eventTopic.publish1(List(PipelineInfo(event.pipelineName, "running")))
          response <- Ok(pipelineId)
        } yield response
      case req @ POST -> Root / "finish"  =>
        for {
          event <- req.as[PipelineFinish]
          eventId <- pipelines.pipelineFinish(event)
          _ <- eventTopic.publish1(List(PipelineInfo("debug", "finish")))
          response <- Ok(eventId)
        } yield response
      case req @ POST -> Root / "failed" =>
        for {
          event <- req.as[PipelineFailed]
          pipelineId <- pipelines.pipelineFailed(event)
          _ <- eventTopic.publish1(List(PipelineInfo("debug", s"failed with error: ${event.error}")))
          response <- Ok(pipelineId)
        } yield response
      case GET -> Root / "info" =>
        WebSocketBuilder[F].build(
          receive = _.map(_ => ()),
          send = eventTopic
            .subscribe(maxQueued = 1000)
            .collect { case event => WebSocketFrame.Text(event.asJson.toString()) }
        )
    }
  }

  def routes(eventTopic: Topic[F, List[PipelineInfo]]): HttpRoutes[F] = {
    Router(
      prefixPath -> httpRoutes(eventTopic)
    )
  }
}