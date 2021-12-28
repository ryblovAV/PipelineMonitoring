package model

object PipelineEventType extends Enumeration {
  val start, finish, failed = Value
}

object DataSourceEventType extends Enumeration {
  val read, write = Value
}

sealed trait MonitoringEvent
final case class PipelineJobEvent(eventType: PipelineEventType.Value)                extends MonitoringEvent
final case class DataSourceEvent(eventType: DataSourceEventType.Value, path: String) extends MonitoringEvent
