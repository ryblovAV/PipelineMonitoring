package monitoring.routes

import cats.MonadThrow
import cats.effect.Sync
import cats.syntax.all._
import model.Events.DataSourceEvent
import model.EventCodecs._
import monitoring.services.{DataSources, Pipelines}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router


final case class DataSourceRoutes[F[_] : Sync: MonadThrow](dataSources: DataSources[F],
                                                           pipelines: Pipelines[F]
                                              ) extends Http4sDsl[F] {

  import org.http4s.circe.CirceEntityCodec._

  private val prefixPath = "/datasources"

  private val httpRoutes = HttpRoutes.of[F] {

    case req @ POST -> Root / "read" =>
      for {
        event <- req.as[DataSourceEvent]
        eventId <- dataSources.dataSourceEvent(event)
        response <- Ok(eventId)
      } yield response

    case req @ POST -> Root / "write" =>
      for {
        event <- req.as[DataSourceEvent]
        eventId <- dataSources.dataSourceEvent(event)
        response <- Ok(eventId)
      } yield response
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
