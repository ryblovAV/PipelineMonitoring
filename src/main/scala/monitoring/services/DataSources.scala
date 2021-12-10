package monitoring.services

import cats.Applicative
import cats.syntax.applicative._
import monitoring.domain.DataSource

trait DataSources[F[_]] {
  def all: F[List[DataSource]]
}

object DataSources {
  def make[F[_]: Applicative]: DataSources[F] = {
    new DataSources[F] {
      override def all: F[List[DataSource]] = {
        List(
          DataSource("DebugDataSource-A"),
          DataSource("DebugDataSource-B")
        ).pure
      }
    }
  }
}