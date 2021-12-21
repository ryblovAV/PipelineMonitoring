package app

import cats.effect.IO
import org.apache.spark.sql.SparkSession

object SparkAppIngest extends PipelineApp {

  val sourceA = "/Users/user/data/data-sources/input/source-a"
  val sourceB = "/Users/user/data/data-sources/output/source-b"

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

  override def process: IO[Unit] = {
    for {
      spark <- createSession(sparkAppName)
      df <- read(sourceA, spark)
      _ <- write(df, sourceB)
    } yield ()
  }

  override def sparkAppName: String = "SparkAppIngest"

  override def outputs: List[String] = List(sourceB)
}
