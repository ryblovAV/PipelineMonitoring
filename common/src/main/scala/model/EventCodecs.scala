package model

import model.Events._

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}

object EventCodecs {

  implicit val customConfig: Configuration = Configuration.default

  implicit val dataSourceEventTypeDecoder: Decoder[DataSourceEventType.Value] = Decoder.decodeEnumeration(DataSourceEventType)
  implicit val dataSourceEventTypeEncoder: Encoder[DataSourceEventType.Value] = Encoder.encodeEnumeration(DataSourceEventType)

  implicit val dataSourceEncoder: Encoder[DataSourceEvent] = deriveConfiguredEncoder[DataSourceEvent]
  implicit val dataSourceDecoder: Decoder[DataSourceEvent] = deriveConfiguredDecoder[DataSourceEvent]

}
