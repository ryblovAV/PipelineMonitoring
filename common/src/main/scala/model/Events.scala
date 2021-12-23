package model

object Events {

  //pipelines
  object PipelineEventType extends Enumeration {
    val start, finish, failed, outdated = Value
  }

  sealed trait PipelineEvent

  // TODO add timestamp
  final case class PipelineStart(pipelineName: String) extends PipelineEvent
  final case class PipelineFinish(pipelineId: Int) extends PipelineEvent
  final case class PipelineFailed(pipelineId: Int, error: String) extends PipelineEvent

  //data sources
  object DataSourceEventType extends Enumeration {
    val read, write = Value
  }

  final case class DataSourceEvent(pipelineId: Int, path: String, eventType: DataSourceEventType.Value)
}