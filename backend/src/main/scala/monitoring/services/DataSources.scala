package monitoring.services

import cats.effect.BracketThrow
import doobie.Transactor
import doobie.implicits._
import monitoring.db.DataSourceDB._
import monitoring.db.Domain.DataSource

trait DataSources[F[_]] {
  def getDataSources: F[List[DataSource]]
  def dataSourceRead(pipelineJobName: String, path: String): F[Unit]
  def dataSourceWrite(pipelineJobName: String, path: String): F[Unit]
}

object DataSources {
  def make[F[_]: BracketThrow](transactor: Transactor[F]): DataSources[F] =
    new DataSources[F] {
      override def dataSourceRead(pipelineJobName: String, path: String): F[Unit] =
        addDataSourceInput(pipelineJobName, path).transact(transactor)

      override def dataSourceWrite(pipelineJobName: String, path: String): F[Unit] =
        addDataSourceOutput(pipelineJobName, path).transact(transactor)

      override def getDataSources: F[List[DataSource]] =
        selectDataSources.transact(transactor)
    }
}
