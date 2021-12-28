package monitoring.modules

import cats.effect.Sync
import cats.syntax.all._
import fs2.kafka.CommittableConsumerRecord
import io.circe.parser.decode
import model.EventCodecs._
import model.{ DataSourceEvent, DataSourceEventType, MonitoringEvent, PipelineEventType, PipelineJobEvent }

trait EventProcessor[F[_]] {
  def processRecord(record: CommittableConsumerRecord[F, String, String]): F[Unit]
}

object EventProcessor {
  def make[F[_]: Sync](services: Services[F]): EventProcessor[F] =
    new EventProcessor[F] {
      override def processRecord(record: CommittableConsumerRecord[F, String, String]): F[Unit] = {
        val pipelineJobName = record.record.key

        Sync[F].delay(decode[MonitoringEvent](record.record.value)).flatMap {
          case Right(PipelineJobEvent(PipelineEventType.start)) => services.pipelines.pipelineStart(pipelineJobName)
          case Right(PipelineJobEvent(eventType)) =>
            services.pipelines.updatePipelineEventType(pipelineJobName, eventType)
          case Right(DataSourceEvent(DataSourceEventType.read, path)) =>
            services.dataSources.dataSourceRead(pipelineJobName, path)
          case Right(DataSourceEvent(DataSourceEventType.write, path)) =>
            services.dataSources.dataSourceWrite(pipelineJobName, path)
          case _ => Sync[F].pure(())
        }

      }
    }

}
