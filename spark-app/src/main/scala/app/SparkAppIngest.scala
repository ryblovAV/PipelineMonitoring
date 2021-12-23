package app

import cats.effect.IO
import org.apache.spark.sql.SparkSession

object SparkAppIngest extends PipelineApp {

  override def pipelineName: String = "SparkAppIngest"

  private val sourceA = "/Users/user/data/data-sources/input/source-a"
  private val sourceB = "/Users/user/data/data-sources/output/source-b"

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

  override def process(pipelineId: Int): IO[Unit] = {
    for {
      spark <- createSession(pipelineName)
      df <- read(pipelineId, sourceA, spark)
      _ <- write(pipelineId, df, sourceB)
    } yield ()
  }


}
