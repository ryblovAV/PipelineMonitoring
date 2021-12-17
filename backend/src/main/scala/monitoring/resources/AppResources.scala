package monitoring.resources

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}
import monitoring.config.{AppConfig, DBConfig}


sealed abstract class AppResources[F[_]](val postgres: Transactor[F])

object AppResources {
  private def makeDBResources[F[_]: ContextShift: Async](config: DBConfig): Resource[F, Transactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](10)
      be <- Blocker[F]
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = config.driverName,
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = ce, // await connection on this EC
        blocker = be, // execute JDBC operations on this EC
      )
    } yield xa

  def make[F[_]: ContextShift: Async](config: AppConfig): Resource[F, AppResources[F]] = {
    for {
      postgres <- makeDBResources(config.dbConfig)
    } yield new AppResources[F](postgres) {}
  }
}