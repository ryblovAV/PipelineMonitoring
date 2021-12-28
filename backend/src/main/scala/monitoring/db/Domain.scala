package monitoring.db

import model.PipelineEventType

object Domain {
  type PipelineJobId = Int
  type DataSourceId  = Int

  case class DataSource(id: Int, path: String)
  case class PipelineJob(pipelineJobId: PipelineJobId, name: String, eventType: PipelineEventType.Value)
  case class DataSourceConnection(dataSourceId: DataSourceId, pipelineJobId: PipelineJobId)
}
