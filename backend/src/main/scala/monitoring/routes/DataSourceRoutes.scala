package monitoring.routes

import cats.MonadThrow
import cats.effect.Sync
import monitoring.services.{ DataSources, Pipelines }
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import io.circe.generic.auto._

final case class DataSourceRoutes[F[_]: Sync: MonadThrow](dataSources: DataSources[F], pipelines: Pipelines[F])
    extends Http4sDsl[F] {

  private val prefixPath = "/datasources"

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root => Ok(dataSources.getDataSources)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
