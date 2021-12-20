package monitoring.routes

import cats.effect.Sync
import cats.syntax.all._
import monitoring.domain.Codecs._
import monitoring.services.Pipelines
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.{Header, HttpRoutes}


final case class PipelineRoutes[F[_]: Sync](pipelines: Pipelines[F]
                                                   ) extends Http4sDsl[F] {

  private val prefixPath = "/pipelines"

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(pipelines.getAllJobs)
        .map(response =>
          response.copy(headers = response.headers.put(Header("Access-Control-Allow-Origin", "*")))
        )
    case req @ POST -> Root / "add"  =>
      req.as[String].flatMap(jobName => Ok(pipelines.addJob(jobName)))
    case GET -> Root / "info" =>
      WebSocketBuilder[F].build(
        receive = ???,
        send = ???
      )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}