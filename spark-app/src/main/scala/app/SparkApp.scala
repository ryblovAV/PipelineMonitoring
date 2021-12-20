package app

import cats.effect.{IO, IOApp}
import org.apache.spark.sql.{SaveMode, SparkSession}
import util.DataFrameReaderExtensions._
import util.DataFrameWriterExtensions._

object SparkApp extends IOApp.Simple {

  def createSession(sparkAppName: String): IO[SparkSession] = {
    IO(
      SparkSession
        .builder()
        .appName(sparkAppName)
        .master("local[*]")
        .getOrCreate()
        .newSession()
    )
  }

  override def run: IO[Unit] = {

    val inputPath = "/Users/user/data/data-sources/input/source-a"
    val outputPath = "/Users/user/data/data-sources/output/source-b"

    for {
      spark <- createSession("testApp")
      reader <- IO(spark.read.format("parquet"))
      df <- reader.loadWithMonitoring(inputPath)
      writer <- IO(df.write.format("parquet").mode(SaveMode.Overwrite))
      count <- writer.saveWithMonitoring(outputPath, spark)
      _ <- IO(println(s"count=$count"))
    } yield ()
  }
}
