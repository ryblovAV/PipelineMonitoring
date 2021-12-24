package monitoring.services

import cats.MonadThrow
import cats.effect.BracketThrow
import doobie.Transactor
import doobie.implicits._
import model.Events._
import monitoring.util.DBUtil._


trait Pipelines[F[_]] {
  def pipelineStart(event: PipelineStart): F[Int]
  def pipelineFinish(event: PipelineFinish): F[Int]
  def pipelineFailed(event: PipelineFailed): F[Int]
  def getPipelineInfos: F[List[PipelineInfo]]
}

object Pipelines {

  def make[F[_] : MonadThrow : BracketThrow](transactor: Transactor[F]): Pipelines[F] = {
    new Pipelines[F] {

      override def pipelineStart(event: PipelineStart): F[Int] = {
        (
          for {
            pipelineId <- insertPipeline(event.pipelineName)
            _ <- insertPipelineEvent(pipelineId, PipelineEventType.start)
          } yield pipelineId
        ).transact(transactor)
      }

      override def pipelineFinish(event: PipelineFinish): F[Int] = {
        insertPipelineEvent(event.pipelineId, PipelineEventType.finish).transact(transactor)
      }

      override def pipelineFailed(event: PipelineFailed): F[Int] = {
        insertPipelineEvent(event.pipelineId, PipelineEventType.failed).transact(transactor)
      }

      override def getPipelineInfos: F[List[PipelineInfo]] = {
        queryPipelineInfos.transact(transactor)
      }
    }
  }
}