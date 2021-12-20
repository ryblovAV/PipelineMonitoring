package util

import cats.effect.{Blocker, ContextShift, IO, Sync}
import org.apache.spark.sql.{DataFrame, DataFrameReader}

object DataFrameReaderExtensions {
  implicit class DataFrameReaderExtensions(reader: DataFrameReader) {
    def loadWithMonitoring(path: String)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[DataFrame] = {
      Blocker[IO].use(blocker => blocker.delay(reader.load(path)))
    }
  }
}