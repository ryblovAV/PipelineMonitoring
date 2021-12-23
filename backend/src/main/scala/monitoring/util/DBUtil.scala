package monitoring.util
import doobie.{ConnectionIO, Meta}
import doobie.implicits._
import doobie.postgres.implicits.pgEnum
import model.Events.{DataSourceEventType, PipelineEventType}

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
}
