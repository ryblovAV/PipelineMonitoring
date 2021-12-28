package monitoring.db

import cats.data.OptionT
import doobie.implicits._
import doobie.postgres.implicits.pgEnum
import doobie.{ ConnectionIO, Meta }
import model.DataSourceEventType
import monitoring.db.Domain.DataSource
import monitoring.db.PipelineDB._

object DataSourceDB {

  implicit val DataSourceEventTypeMeta: Meta[DataSourceEventType.Value] =
    pgEnum(DataSourceEventType, "datasource_event_type")

  def selectDataSources: ConnectionIO[List[DataSource]] =
    sql"select id, path from data_source".query[DataSource].to[List]

  def selectDataSourceByPath(path: String): ConnectionIO[Option[Int]] =
    sql"select id from data_source where path = $path".query[Int].option

  def selectDataSourceOutput(pipelineJobId: Int, dataSourceId: Int): ConnectionIO[Option[Int]] =
    sql"select id from data_source_output where pipeline_job_id = $pipelineJobId and data_source_id = $dataSourceId"
      .query[Int]
      .option

  def deleteDataSourceOutput(dataSourceId: Int): ConnectionIO[Int] =
    sql"delete from data_source_output where data_source_id = $dataSourceId".update.run

  def deleteDataSourceInputs(pipelineJobId: Int): ConnectionIO[Int] =
    sql"delete from data_source_input where pipeline_job_id = $pipelineJobId".update.run

  def getOrInsertDataSource(path: String): ConnectionIO[Int] =
    OptionT(
      selectDataSourceByPath(path)
    ).getOrElseF(
      sql"insert into data_source (path) values ($path)".update
        .withGeneratedKeys[Int]("id")
        .compile
        .lastOrError
    )

  def insertDataSourceInput(pipelineJobId: Int, dataSourceId: Int): ConnectionIO[Int] =
    sql"insert into data_source_input (pipeline_job_id, data_source_id) values ($pipelineJobId, $dataSourceId)".update
      .withGeneratedKeys[Int]("id")
      .compile
      .lastOrError

  def insertDataSourceOutput(pipelineJobId: Int, dataSourceId: Int): ConnectionIO[Int] =
    sql"insert into data_source_output (pipeline_job_id, data_source_id) values ($pipelineJobId, $dataSourceId)".update
      .withGeneratedKeys[Int]("id")
      .compile
      .lastOrError

  def addDataSourceInput(pipelineJobName: String, path: String): ConnectionIO[Unit] =
    for {
      pipelineJobId <- getOrInsertPipelineJob(pipelineJobName)
      dataSourceId  <- getOrInsertDataSource(path)
      _             <- insertDataSourceInput(pipelineJobId, dataSourceId)
    } yield ()

  def addDataSourceOutput(pipelineJobName: String, path: String): ConnectionIO[Unit] =
    for {
      pipelineJobId <- getOrInsertPipelineJob(pipelineJobName)
      dataSourceId  <- getOrInsertDataSource(path)
      _             <- deleteDataSourceOutput(dataSourceId)
      _             <- insertDataSourceOutput(pipelineJobId, dataSourceId)
    } yield ()
}
