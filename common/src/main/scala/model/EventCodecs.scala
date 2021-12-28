package model

import cats.syntax.all._
import io.circe.generic.extras.Configuration
import io.circe.syntax._
import io.circe.{ Decoder, Encoder }

object EventCodecs {

  import io.circe.generic.extras.semiauto._

  implicit val customConfig: Configuration = Configuration.default

  implicit val pipelineEventTypeDecoder: Decoder[PipelineEventType.Value] =
    Decoder.decodeEnumeration(PipelineEventType)
  implicit val pipelineEventTypeEncoder: Encoder[PipelineEventType.Value] =
    Encoder.encodeEnumeration(PipelineEventType)

  implicit val pipelineJobEventDecoder: Decoder[PipelineJobEvent] = deriveConfiguredDecoder[PipelineJobEvent]
  implicit val pipelineJobEventEncoder: Encoder[PipelineJobEvent] = deriveConfiguredEncoder[PipelineJobEvent]

  implicit val dataSourceEventTypeDecoder: Decoder[DataSourceEventType.Value] =
    Decoder.decodeEnumeration(DataSourceEventType)
  implicit val dataSourceEventTypeEncoder: Encoder[DataSourceEventType.Value] =
    Encoder.encodeEnumeration(DataSourceEventType)

  implicit val dataSourceEventDecoder: Decoder[DataSourceEvent] = deriveConfiguredDecoder[DataSourceEvent]
  implicit val dataSourceEventEncoder: Encoder[DataSourceEvent] = deriveConfiguredEncoder[DataSourceEvent]

  implicit val monitoringEventDecoder: Decoder[MonitoringEvent] =
    List[Decoder[MonitoringEvent]](pipelineJobEventDecoder.widen, dataSourceEventDecoder.widen).reduceLeft(_ or _)
  implicit val monitoringEventEncoder: Encoder[MonitoringEvent] = Encoder.instance {
    case pipelineJobEvent: PipelineJobEvent => pipelineJobEvent.asJson
    case dataSourceEvent: DataSourceEvent   => dataSourceEvent.asJson
  }
}
