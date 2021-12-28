package monitoring.db

import cats.data.OptionT
import doobie.implicits._
import doobie.postgres.implicits.pgEnum
import doobie.{ ConnectionIO, Meta }
import model.{ PipelineEventType, PipelineInfo }
import monitoring.db.Domain.{ DataSourceConnection, PipelineJob }
import cats.syntax.all._
import cats.effect.implicits._

object PipelineDB {

  implicit val PipelineEventTypeMeta: Meta[PipelineEventType.Value] = pgEnum(PipelineEventType, "pipeline_event_type")

  def selectPipelineJobByName(jobName: String): ConnectionIO[Option[Int]] =
    sql"select id from pipeline_job where name = $jobName".query[Int].option

  def getOrInsertPipelineJob(jobName: String): ConnectionIO[Int] =
    OptionT(
      selectPipelineJobByName(jobName)
    ).getOrElseF(
      sql"insert into pipeline_job (name) values ($jobName)".update
        .withGeneratedKeys[Int]("id")
        .compile
        .lastOrError
    )

  def updatePipelineJobEventType(jobName: String, eventType: PipelineEventType.Value): ConnectionIO[Int] =
    OptionT(
      sql"update pipeline_job set event_type = $eventType where name = $jobName".update
        .withGeneratedKeys[Int]("id")
        .compile
        .last
    ).getOrElseF(
      sql"insert into pipeline_job (name, event_type) values ($jobName, $eventType)".update
        .withGeneratedKeys[Int]("id")
        .compile
        .lastOrError
    )

  def queryPipelineInfos: ConnectionIO[List[PipelineInfo]] =
    sql"""
        select name, event_type
          from pipeline_job
      order by name""".query[PipelineInfo].to[List]

  def queryData: ConnectionIO[(List[PipelineJob], List[DataSourceConnection], List[DataSourceConnection])] =
    for {
      pipelineJobs <- sql"select id, name, event_type from pipeline_job".query[PipelineJob].to[List]
      inputs       <- sql"select data_source_id, pipeline_job_id from data_source_input".query[DataSourceConnection].to[List]
      outputs <- sql"select data_source_id, pipeline_job_id from data_source_output"
        .query[DataSourceConnection]
        .to[List]
    } yield (pipelineJobs, inputs, outputs)

}
