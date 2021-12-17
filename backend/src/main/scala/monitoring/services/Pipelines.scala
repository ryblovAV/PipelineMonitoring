package monitoring.services

import cats.MonadThrow
import cats.effect.BracketThrow
import doobie.Transactor
import doobie.implicits._
import monitoring.domain.Connection.ConnectionType
import monitoring.domain.{Connection, PipelineJob}
import monitoring.util.DBUtil._


trait Pipelines[F[_]] {
  def getAllJobs: F[List[PipelineJob]]

  def addJob(jobName: String): F[Int]

  def addJobDataSource(pipelineJobId: Int,
                       dataSourcePath: String,
                       connectionType: ConnectionType.Value): F[Int]

}

object Pipelines {

  def make[F[_] : MonadThrow : BracketThrow](transactor: Transactor[F]): Pipelines[F] = {
    new Pipelines[F] {

      override def addJob(jobName: String): F[Int] = {
        insertPipelineJob(jobName).transact(transactor)
      }

      override def getAllJobs: F[List[PipelineJob]] = {
        selectAllPipelineJobs.transact(transactor)
      }

      override def addJobDataSource(pipelineJobId: Int,
                                    dataSourcePath: String,
                                    connectionType: Connection.ConnectionType.Value): F[Int] = {
        (
          for {
            dataSourceId <- insertDataSource(dataSourcePath)
            connectionId <- insertConnection(pipelineJobId, dataSourceId, connectionType)
          } yield connectionId
        ).transact(transactor)
      }
    }
  }
}