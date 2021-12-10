package monitoring.domain

case class PipelineJob(name: String,
                       inputs: List[DataSource],
                       outputs: List[DataSource],
                       isFailed: Boolean = false)