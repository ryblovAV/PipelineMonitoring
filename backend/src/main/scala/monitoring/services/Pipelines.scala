package monitoring.services

import cats.MonadThrow
import cats.effect.BracketThrow
import cats.syntax.all._
import doobie.Transactor
import doobie.implicits._
import fs2.concurrent.Topic
import model.{ PipelineEventType, PipelineInfo }
import monitoring.db.DataSourceDB
import monitoring.db.PipelineDB._

trait Pipelines[F[_]] {

  def pipelineInfoStream: fs2.Stream[F, List[PipelineInfo]]

  def updatePipelineEventType(pipelineJobName: String, eventType: PipelineEventType.Value): F[Unit]

  def pipelineStart(pipelineJobName: String): F[Unit]

  def getPipelineInfos: F[List[PipelineInfo]]
}

object Pipelines {

  def make[F[_]: MonadThrow: BracketThrow](
      transactor: Transactor[F],
      eventTopic: Topic[F, List[PipelineInfo]]
  ): Pipelines[F] =
    new Pipelines[F] {

      override def pipelineInfoStream: fs2.Stream[F, List[PipelineInfo]] =
        eventTopic.subscribe(maxQueued = 1000)

      override def updatePipelineEventType(pipelineJobName: String, eventType: PipelineEventType.Value): F[Unit] =
        for {
          _             <- updatePipelineJobEventType(pipelineJobName, eventType).transact(transactor)
          pipelineInfos <- getPipelineInfos
          _             <- eventTopic.publish1(pipelineInfos)
        } yield ()

      override def pipelineStart(pipelineJobName: String): F[Unit] = {
        val db = for {
          pipelineJobId <- updatePipelineJobEventType(pipelineJobName, PipelineEventType.start)
          _             <- DataSourceDB.deleteDataSourceInputs(pipelineJobId)
        } yield ()

        for {
          _             <- db.transact(transactor)
          pipelineInfos <- getPipelineInfos
          _             <- eventTopic.publish1(pipelineInfos)
        } yield ()
      }

      override def getPipelineInfos: F[List[PipelineInfo]] =
        queryData.transact(transactor).map {
          case (pipelineJobs, inputs, outputs) =>
            DependencyChecker
              .run(pipelineJobs, inputs, outputs)
              .map {
                case (p, state) =>
                  PipelineInfo(
                    p.name,
                    s"${p.eventType}, inputs: ${p.inputs.map(_.name).distinct.sorted.mkString("; ")}, state = $state"
                  )
              }
              .sortBy(_.pipelineName)
        }
    }
}
