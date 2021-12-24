package monitoring.services

import cats.effect.BracketThrow
import doobie.Transactor
import doobie.implicits._
import monitoring.domain.DataSource


trait DataSources[F[_]] {
  def all: F[List[DataSource]]
}
object DataSources {
  def make[F[_]: BracketThrow](transactor: Transactor[F]): DataSources[F] = {
    new DataSources[F] {
      override def all: F[List[DataSource]] = {
          sql"select id, path from data_source".query[DataSource].to[List].transact(transactor)
      }
    }
  }
}