package monitoring.modules

import cats.effect.Sync
import cats.syntax.all._
import monitoring.routes.{DataSourceRoutes, PipelineRoutes}
import org.http4s.implicits._
import org.http4s.{HttpApp, HttpRoutes}


object HttpApi {
  def make[F[_]: Sync](services: Services[F]): HttpApi[F] = {
    new HttpApi[F](services) {}
  }
}

sealed abstract class HttpApi[F[_]: Sync] private (services: Services[F]) {
  private val pipelineRoutes: HttpRoutes[F] = PipelineRoutes[F](services.pipelines).routes
  private val dataSourcesRoutes: HttpRoutes[F] = DataSourceRoutes[F](services.dataSources, services.pipelines).routes

  private val routes: HttpRoutes[F] = pipelineRoutes <+> dataSourcesRoutes

  val httpApp: HttpApp[F] = routes.orNotFound
}