package monitoring.modules

import cats.effect.{Concurrent, Sync}
import cats.syntax.all._
import model.PipelineStatusEvent
import fs2.concurrent.Topic
import monitoring.routes.{DataSourceRoutes, PipelineRoutes}
import org.http4s.implicits._
import org.http4s.{HttpApp, HttpRoutes}


object HttpApi {
  def make[F[_]: Sync : Concurrent](services: Services[F]): HttpApi[F] = {
    new HttpApi[F](services) {}
  }
}

sealed abstract class HttpApi[F[_]: Sync: Concurrent] private(services: Services[F]) {
  private def pipelineRoutes(eventTopic: Topic[F, Option[PipelineStatusEvent]]): HttpRoutes[F] = PipelineRoutes[F](services.pipelines).routes(eventTopic)
  private val dataSourcesRoutes: HttpRoutes[F] = DataSourceRoutes[F](services.dataSources, services.pipelines).routes

  private def routes(eventTopic: Topic[F, Option[PipelineStatusEvent]]): HttpRoutes[F] = {
    pipelineRoutes(eventTopic) <+> dataSourcesRoutes
  }

  def httpApp(eventTopic: Topic[F, Option[PipelineStatusEvent]]): HttpApp[F] = routes(eventTopic).orNotFound
}