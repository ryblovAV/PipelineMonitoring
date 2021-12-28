package app
import org.apache.spark.sql.functions._
import cats.effect.IO
import cats.syntax.all._
import monitoring.MonitoringClient
import org.apache.spark.sql.SaveMode

case class PipelineSimpleJob(pipelineJobName: String, paths: Seq[(String, String)], isFailed: Boolean = false)
    extends BasePipelineJob {
  override def process(monitoringClient: MonitoringClient): IO[Unit] = {
    val dataStorageFormat = "parquet"
    for {
      spark <- createSession(pipelineJobName)
      _     <- if (isFailed) IO.raiseError(new Exception) else IO.pure(())
      _ <- paths.map {
        case (inputPath, outputPath) =>
          read(spark.read.format(dataStorageFormat), inputPath, monitoringClient)
            .flatMap(
              df =>
                write(
                  df.withColumn("name", lit(pipelineJobName))
                    .write
                    .format(dataStorageFormat)
                    .mode(SaveMode.Overwrite),
                  outputPath,
                  monitoringClient
                )
            )
      }.sequence
    } yield ()
  }
}
