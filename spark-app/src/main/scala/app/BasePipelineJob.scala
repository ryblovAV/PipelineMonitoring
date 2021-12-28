package app

import cats.effect.{ Blocker, ContextShift, IO, IOApp, Sync }
import monitoring.MonitoringClient
import model.{ DataSourceEvent, DataSourceEventType, PipelineEventType, PipelineJobEvent }
import org.apache.spark.sql.{ DataFrame, DataFrameReader, DataFrameWriter, SparkSession }

trait BasePipelineJob extends IOApp.Simple {

  def pipelineJobName: String

  def readWithoutMonitoring(
      path: String,
      spark: SparkSession
  )(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[DataFrame] =
    Blocker[IO].use(_.delay(spark.read.format("parquet").load(path)))

  def createSession(sparkAppName: String): IO[SparkSession] =
    IO(
      SparkSession
        .builder()
        .appName(sparkAppName)
        .master("local[*]")
        .getOrCreate()
        .newSession()
    )

  def read(reader: => DataFrameReader, path: String, monitoringClient: MonitoringClient)(
      implicit contextShift: ContextShift[IO],
      sync: Sync[IO]
  ): IO[DataFrame] =
    for {
      _  <- monitoringClient.send(pipelineJobName, DataSourceEvent(DataSourceEventType.read, path))
      df <- Blocker[IO].use(_.delay(reader.load(path)))
    } yield df

  def write[T](writer: => DataFrameWriter[T], path: String, monitoringClient: MonitoringClient)(
      implicit contextShift: ContextShift[IO],
      sync: Sync[IO]
  ): IO[Unit] =
    for {
      _ <- Blocker[IO].use(_.delay(writer.save(path)))
      _ <- monitoringClient.send(pipelineJobName, DataSourceEvent(DataSourceEventType.write, path))
    } yield ()

  def process(monitoringCLient: MonitoringClient): IO[Unit]

  override def run: IO[Unit] =
    for {
      monitoringClient <- MonitoringClient.make
      _                <- monitoringClient.send(pipelineJobName, PipelineJobEvent(PipelineEventType.start))
      _ <- process(monitoringClient)
        .handleErrorWith(
          error =>
            monitoringClient.send(pipelineJobName, PipelineJobEvent(PipelineEventType.failed)) *> IO
              .raiseError(error)
        )
      _ <- monitoringClient.send(pipelineJobName, PipelineJobEvent(PipelineEventType.finish))
    } yield ()
}
