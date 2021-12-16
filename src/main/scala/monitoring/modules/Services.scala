package monitoring.modules

import cats.effect.BracketThrow
import doobie.util.transactor.Transactor
import monitoring.services.{DataSources, Pipelines}

object Services {

  def make[F[_]: BracketThrow](postgres: Transactor[F]): Services[F] = {
    new Services[F](
      Pipelines.make[F](postgres),
      DataSources.make[F](postgres)
    ) {}
  }

}

sealed abstract class Services[F[_]] private(val pipelines: Pipelines[F],
                                             val dataSources: DataSources[F])
