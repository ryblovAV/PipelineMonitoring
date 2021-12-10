package monitoring.routes

import cats.{Defer, Monad}
import monitoring.services.Pipelines
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import monitoring.domain.Codecs._
import org.http4s.server.Router

final case class PipelineRoutes[F[_]: Monad: Defer](pipelines: Pipelines[F]
                                                   ) extends Http4sDsl[F] {

  private val prefixPath = "/pipelines"

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root => Ok(pipelines.all)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}