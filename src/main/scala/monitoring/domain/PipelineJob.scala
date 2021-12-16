package monitoring.domain

final case class DataSource(id: Int, path: String)

final case class PipelineJob(id: Int,
                             name: String,
                             inputs: List[DataSource],
                             outputs: List[DataSource])