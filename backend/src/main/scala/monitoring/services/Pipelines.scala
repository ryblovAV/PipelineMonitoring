package monitoring.services

import cats.MonadThrow
import cats.effect.BracketThrow
import model.SparkAppStart
import doobie.{ConnectionIO, Transactor}
import doobie.implicits._
import monitoring.domain.Connection.ConnectionType
import monitoring.domain.{Connection, PipelineJob}
import monitoring.util.DBUtil._
import cats.syntax.all._


trait Pipelines[F[_]] {
  def getAllJobs: F[List[PipelineJob]]

  def addJob(jobName: String): F[Int]

  def startApp(sparkAppStart: SparkAppStart): F[Int]

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

      override def startApp(sparkAppStart: SparkAppStart): F[Int] = {
        (
          for {
            dataSourceIds <- sparkAppStart.outputs.map(insertDataSource).sequence
            pipelineJobId <- insertPipelineJob(sparkAppStart.pipelineName)
            _ <- dataSourceIds.map(dataSourceId => insertConnection(pipelineJobId, dataSourceId, ConnectionType.output)).sequence
          } yield pipelineJobId
        ).transact(transactor)
      }
    }
  }
}