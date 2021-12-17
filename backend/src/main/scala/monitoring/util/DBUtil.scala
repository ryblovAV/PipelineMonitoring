package monitoring.util
import cats.data.OptionT
import doobie.implicits._
import doobie.postgres.implicits.pgEnum
import doobie.{ConnectionIO, Meta}
import monitoring.domain.Connection.ConnectionType
import monitoring.domain.{DataSource, PipelineJob}

object DBUtil {

  implicit val ConnectionTypeMeta: Meta[ConnectionType.Value] = pgEnum(ConnectionType, "connection_type")

  case class JobRawAttr(jobId: Int,
                        jobName: String,
                        dataSourceId: Option[Int],
                        dataSourcePath: Option[String],
                        connectionType: Option[ConnectionType.Value])

  val selectRawJobAttrs: ConnectionIO[List[JobRawAttr]] = {
    sql"""
       select j.id, j.name, c.data_source_id, c.path, c.connection_type
         from pipeline_job j
         left outer join (
            select c.pipeline_job_id, c.connection_type, c.data_source_id, d.path
              from connection c, data_source d
             where d.id = c.data_source_id) c on c.pipeline_job_id = j.id
     """.query[JobRawAttr].to[List]
  }

  def selectAllPipelineJobs: ConnectionIO[List[PipelineJob]] = {
    selectRawJobAttrs.map { jobAttrs =>
      jobAttrs
        .groupBy(attr => (attr.jobId, attr.jobName))
        .map { case ((jobId, jobName), attrs) =>

          val (inputs, outputs) = (
            for {
              attr <- attrs
              dataSourceId <- attr.dataSourceId
              dataSourcePath <- attr.dataSourcePath
              connectionType <- attr.connectionType
            } yield (connectionType, DataSource(dataSourceId, dataSourcePath))
          ).partition { case (connectionType, _) =>
            connectionType == ConnectionType.input
          }

          PipelineJob(
            jobId,
            jobName,
            inputs.map { case (_, dataSource) => dataSource },
            outputs.map { case (_, dataSource) => dataSource }
          )
        }.toList
    }
  }

  def selectPipelineJobByName(jobName: String): ConnectionIO[Option[Int]] = {
    sql"select id from pipeline_job where name = $jobName".query[Int].option
  }

  def selectDataSourceByPath(path: String): ConnectionIO[Option[Int]] = {
    sql"select id from data_source where path = $path".query[Int].option
  }

  def insertPipelineJob(jobName: String): doobie.ConnectionIO[Int] = {
    OptionT(
        selectPipelineJobByName(jobName)
      )
      .getOrElseF(
        sql"insert into pipeline_job (name) values ($jobName)"
          .update
          .withGeneratedKeys[Int]("id")
          .compile
          .lastOrError
      )
  }

  def insertDataSource(path: String): ConnectionIO[Int] = {
    OptionT(
      selectDataSourceByPath(path)
    )
      .getOrElseF(
        sql"""
          insert into data_source (path)
          values ($path)
        """
          .update
          .withGeneratedKeys[Int]("id")
          .compile
          .lastOrError
      )
  }

  def insertConnection(pipelineJobId: Int,
                       dataSourceId: Int,
                       connectionType: ConnectionType.Value
                      ): ConnectionIO[Int] = {
    OptionT(
      sql"""
        select id
          from connection
         where pipeline_job_id = $pipelineJobId
           and data_source_id = $dataSourceId
           and connection_type = $connectionType
      """.query[Int].option
    )
      .getOrElseF(
        sql"""
          insert into connection (pipeline_job_id, data_source_id, connection_type)
          values ($pipelineJobId, $dataSourceId, $connectionType)
        """
          .update
          .withGeneratedKeys[Int]("id")
          .compile
          .lastOrError
      )
  }
}
