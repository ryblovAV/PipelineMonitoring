package model

final case class JobDataSourceAttr(pipelineJobId: Int, dataSourcePath: String)

final case class DataSourceRead(jobName: String, dataSourcePath: String)
final case class SparkAppStart(pipelineName: String, outputs: List[String])
final case class SparkAppEnd(jobName: String)
final case class SparkAppFailed(jobName: String, error: String)


final case class PipelineStatusEvent(jobName: String, status: String)

case class ClientCommand(name: String)