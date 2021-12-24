package monitoring.util
import doobie.{ConnectionIO, Meta}
import doobie.implicits._
import doobie.postgres.implicits.pgEnum
import model.Events.{DataSourceEventType, PipelineEventType, PipelineInfo}

object DBUtil {

  implicit val PipelineEventTypeMeta: Meta[PipelineEventType.Value] = pgEnum(PipelineEventType, "pipeline_event_type")
  implicit val DataSourceEventTypeMeta: Meta[DataSourceEventType.Value] = pgEnum(DataSourceEventType, "datasource_event_type")


  def insertPipeline(pipelineName: String): ConnectionIO[Int] = {
    sql"insert into pipeline (name) values ($pipelineName)"
      .update
      .withGeneratedKeys[Int]("id")
      .compile
      .lastOrError
  }

  def insertPipelineEvent(pipelineId: Int, state: PipelineEventType.Value): ConnectionIO[Int] = {
    sql"insert into pipeline_event (pipeline_id, state) values ($pipelineId, $state)"
      .update
      .withGeneratedKeys[Int]("id")
      .compile
      .lastOrError
  }

  def insertDataSourceEvent(pipelineId: Int, eventType: DataSourceEventType.Value): ConnectionIO[Int] = {
    sql"insert into datasource_event (pipeline_id, event_type) values ($pipelineId, $eventType)"
      .update
      .withGeneratedKeys[Int]("id")
      .compile
      .lastOrError
  }

  def queryPipelineInfos: ConnectionIO[List[PipelineInfo]] = {
   sql"""
        select lp.name, lpe.state
        from (select p.id, p.name
                from (
                    select id,
                           name,
                           row_number() over (partition by name order by id desc) as row_number
                     from pipeline
                    ) p
                where p.row_number = 1) lp,
             (select pe.state, pe.event_date, pe.pipeline_id
                from (select pipeline_id,
                             state,
                             event_date,
                             row_number() over (partition by pipeline_id order by id desc) as row_number
                        from pipeline_event
                    ) pe
             where row_number = 1) lpe
        where lp.id = lpe.pipeline_id
        order by lp.name""".query[PipelineInfo].to[List]
  }
}
