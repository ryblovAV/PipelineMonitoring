package monitoring.modules

import cats.effect.BracketThrow
import doobie.util.transactor.Transactor
import fs2.concurrent.Topic
import model.PipelineInfo
import monitoring.services.{ DataSources, Pipelines }

trait Services[F[_]] {
  def pipelines: Pipelines[F]
  def dataSources: DataSources[F]
}

object Services {
  def make[F[_]: BracketThrow](postgres: Transactor[F], eventTopic: Topic[F, List[PipelineInfo]]): Services[F] =
    new Services[F] {
      val pipelines: Pipelines[F]     = Pipelines.make[F](postgres, eventTopic)
      val dataSources: DataSources[F] = DataSources.make[F](postgres)
    }
}
