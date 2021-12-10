package monitoring.routes

import cats.{Defer, Monad}
import monitoring.services.DataSources
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import monitoring.domain.Codecs._
import org.http4s.circe.CirceEntityEncoder._

final case class DataSourceRoutes[F[_]: Monad: Defer](dataSources: DataSources[F]) extends Http4sDsl[F] {

  private val prefixPath = "/datasources"

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root => Ok(dataSources.all)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
