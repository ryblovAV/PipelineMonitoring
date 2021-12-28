package monitoring.modules

import cats.effect.{ Concurrent, Sync }
import cats.syntax.all._
import monitoring.routes.{ DataSourceRoutes, PipelineRoutes }
import org.http4s.implicits._
import org.http4s.{ HttpApp, HttpRoutes }

trait HttpApi[F[_]] {
  def httpApp: HttpApp[F]
}

object HttpApi {

  def make[F[_]: Sync: Concurrent](services: Services[F]): HttpApi[F] =
    new HttpApi[F] {

      private val pipelineRoutes: HttpRoutes[F] =
        PipelineRoutes[F](services.pipelines).routes

      private val dataSourcesRoutes: HttpRoutes[F] =
        DataSourceRoutes[F](services.dataSources, services.pipelines).routes

      private def routes: HttpRoutes[F] =
        pipelineRoutes <+> dataSourcesRoutes

      override def httpApp: HttpApp[F] = routes.orNotFound
    }
}
