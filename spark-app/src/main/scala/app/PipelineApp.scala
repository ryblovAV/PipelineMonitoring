package app

import cats.effect.{Blocker, ContextShift, IO, IOApp, Sync}
import http.MonitoringClient
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}


trait PipelineApp extends IOApp.Simple {

  def sparkAppName: String

  def outputs: List[String]

  def read(path: String, spark: SparkSession)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[DataFrame] = {
    for {
      _ <- MonitoringClient.sendDataSourceRead(sparkAppName, path)
      df <- Blocker[IO].use(blocker =>
        blocker.delay(spark.read.format("parquet").load(path)))
    } yield df
  }

  def write(df: DataFrame, path: String)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[Unit] = {
    Blocker[IO].use(blocker => blocker.delay(df.write.format("parquet").mode(SaveMode.Overwrite).save(path)))
  }

  def process: IO[Unit]

  override def run: IO[Unit] = {
    for {
      _ <- MonitoringClient.sendSparkAppStart(sparkAppName, outputs)
      _ <- process.handleErrorWith(error => MonitoringClient.sendSparkAppFailed(sparkAppName, error) *> IO.raiseError(error))
      _ <- MonitoringClient.sendSparkAppEnd(sparkAppName)
    } yield()
  }

}
