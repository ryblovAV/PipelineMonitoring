package app

import cats.effect.{Blocker, ContextShift, IO, IOApp, Sync}
import http.MonitoringClient
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}


trait PipelineApp extends IOApp.Simple {

  def pipelineName: String

  def read(pipelineId: Int, path: String, spark: SparkSession)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[DataFrame] = {
    for {
      _ <- MonitoringClient.sendDataSourceRead(pipelineId, path)
      df <- Blocker[IO].use(_.delay(spark.read.format("parquet").load(path)))
    } yield df
  }

  def write(pipelineId: Int, df: DataFrame, path: String)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[Unit] = {
    for {
      _ <- Blocker[IO].use(_.delay(df.write.format("parquet").mode(SaveMode.Overwrite).save(path)))
      _ <- MonitoringClient.sendDataSourceWrite(pipelineId, path)
    } yield ()

  }

  def process(pipelineId: Int): IO[Unit]

  override def run: IO[Unit] = {
    for {
      pipelineId <- MonitoringClient.sendSparkAppStart(pipelineName)
      _ <- process(pipelineId)
        .handleErrorWith(error =>
          MonitoringClient.sendSparkAppFailed(pipelineId, error) *> IO.raiseError(error)
        )
      _ <- MonitoringClient.sendSparkAppEnd(pipelineId)
    } yield()
  }

}
