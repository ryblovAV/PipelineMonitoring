package monitoring.routes

import cats.effect.Sync
import cats.syntax.all._
import monitoring.domain.Codecs._
import monitoring.domain.Connection.ConnectionType
import monitoring.services.{DataSources, Pipelines}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request, Response}


final case class JobDataSourceAttr(pipelineJobId: Int, dataSourcePath: String)

final case class DataSourceRoutes[F[_] : Sync](dataSources: DataSources[F],
                                               pipelines: Pipelines[F]
                                              ) extends Http4sDsl[F] {

  import io.circe.generic.auto._
  import org.http4s.circe.CirceEntityCodec._

  private def addDataSource(connectionType: ConnectionType.Value)(req: Request[F]): F[Response[F]] = {
    req.as[JobDataSourceAttr].flatMap { case JobDataSourceAttr(pipelineJobId, dataSourcePath) =>
      Ok(pipelines.addJobDataSource(pipelineJobId, dataSourcePath, connectionType))
    }
  }

  private val prefixPath = "/datasources"

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root => Ok(dataSources.all)
    case req@POST -> Root / "add" / "input" => addDataSource(ConnectionType.input)(req)
    case req@POST -> Root / "add" / "output" => addDataSource(ConnectionType.output)(req)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
