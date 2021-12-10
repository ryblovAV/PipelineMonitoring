package monitoring.domain

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}

object Codecs {

  implicit val customConfig: Configuration = Configuration.default

  implicit val dataSourceEncoder: Encoder[DataSource] = deriveConfiguredEncoder[DataSource]
  implicit val dataSourceDecoder: Decoder[DataSource] = deriveConfiguredDecoder[DataSource]

  implicit val pipelineEncoder: Encoder[PipelineJob] = deriveConfiguredEncoder[PipelineJob]
  implicit val pipelineDecoder: Decoder[PipelineJob] = deriveConfiguredDecoder[PipelineJob]


}
