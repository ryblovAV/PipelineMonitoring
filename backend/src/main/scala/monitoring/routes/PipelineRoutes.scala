package monitoring.routes

import cats.effect.Concurrent
import monitoring.services.Pipelines
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

final case class PipelineRoutes[F[_]: Concurrent](pipelines: Pipelines[F]) extends Http4sDsl[F] {
  import io.circe.generic.auto._
  import io.circe.syntax._

  private val prefixPath = "/pipelines"

  private def httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "info" =>
        WebSocketBuilder[F].build(
          receive = _.map(_ => ()),
          send = pipelines.pipelineInfoStream.collect {
            case event => WebSocketFrame.Text(event.asJson.toString())
          }
        )
    }

  val routes: HttpRoutes[F] =
    Router(
      prefixPath -> httpRoutes
    )
}
