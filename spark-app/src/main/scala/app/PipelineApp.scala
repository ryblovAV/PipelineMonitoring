package app

import cats.effect.{Blocker, ContextShift, IO, IOApp, Sync}
import http.MonitoringClient
import org.apache.spark.sql.{DataFrame, DataFrameReader, Dataset, SaveMode, SparkSession}

trait PipelineApp extends IOApp.Simple {

  def sparkAppName: String

  def outputs: List[String]

  // lets parametrize on on dataframe type
  // not sure we need to wrap in IO, but if effect type like IO is used maybe makes sense to parmetrize on it
  // why put read and write methods here - limits types of spark jobs to simple read/write jobs with no partitioning
  // maybe use wrapper methods for read and write
  // ofc extending DataFrameWriter and overloading load would be nicer, but spark uses private constructors.
  def registeredRead[T]( f: => Dataset[T]): Dataset[T] = {
    //send read event
    f
  }

  def registerWrite[T]( f: Dataset[T] => Unit): Unit= {
    //send write event
    f
  }

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

