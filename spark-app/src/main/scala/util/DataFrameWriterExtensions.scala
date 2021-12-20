package util

import cats.effect.{Blocker, ContextShift, IO, Sync}
import org.apache.spark.scheduler.{SparkListener, SparkListenerTaskEnd}
import org.apache.spark.sql.{DataFrameWriter, SparkSession}

import java.util.concurrent.atomic.AtomicReference
import scala.util.Try

object DataFrameWriterExtensions {

  case class CounterSparkListener(countRef: AtomicReference[Long]) extends SparkListener {
    override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
      if (taskEnd.taskType == "ResultTask") {
        val countWritten = Try(taskEnd.taskMetrics.outputMetrics.recordsWritten).getOrElse(0L)
        countRef.updateAndGet(_ + countWritten)
      }
    }
  }

  implicit class DataFrameWriterExtensions[T](writer: DataFrameWriter[T]) {
    def saveWithMonitoring(path: String, spark: SparkSession)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[Long] = {
      for {
        countRef <- IO(new AtomicReference(0L))
        sparkListener <- IO(CounterSparkListener(countRef))
        _ <- IO(spark.sparkContext.addSparkListener(sparkListener))
        _ <- Blocker[IO].use(blocker => blocker.delay(writer.save(path)))
        count <- IO(sparkListener.countRef.get())
        _ <- IO(spark.sparkContext.removeSparkListener(sparkListener))
      } yield count
    }
  }
}
