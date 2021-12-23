package monitoring.services

import cats.effect.BracketThrow
import doobie.Transactor
import doobie.implicits._
import model.Events.DataSourceEvent
import monitoring.util.DBUtil.insertDataSourceEvent


trait DataSources[F[_]] {
  def dataSourceEvent(event: DataSourceEvent): F[Int]
}

object DataSources {
  def make[F[_]: BracketThrow](transactor: Transactor[F]): DataSources[F] = {
    new DataSources[F] {
      override def dataSourceEvent(event: DataSourceEvent): F[Int] = {
        insertDataSourceEvent(event.pipelineId, event.eventType).transact(transactor)
      }
    }
  }
}