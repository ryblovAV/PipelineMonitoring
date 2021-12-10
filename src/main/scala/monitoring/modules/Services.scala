package monitoring.modules

import cats.MonadThrow
import monitoring.services.{DataSources, Pipelines}

object Services {

  def make[F[_]: MonadThrow]: Services[F] = new Services[F](
    Pipelines.make[F],
    DataSources.make[F]
  ) {}

}

sealed abstract class Services[F[_]] private(val pipelines: Pipelines[F],
                                             val dataSources: DataSources[F])
