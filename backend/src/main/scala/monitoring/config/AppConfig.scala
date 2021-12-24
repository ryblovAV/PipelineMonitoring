package monitoring.config

import cats.effect.Sync


case class DBConfig(driverName: String,
                    url: String,
                    user: String,
                    password: String)

case class AppConfig(dbConfig: DBConfig)

object AppConfig {

  val default: AppConfig = AppConfig(
    DBConfig(
      driverName = "org.postgresql.Driver",
      url = "jdbc:postgresql:job_monitoring",
      user = "docker",
      password = "docker"
    )
  )

  // lets read apoConfig with pure config, ie smth like
    def load[F[_]: Sync]: F[AppConfig] =
      Sync[F].delay(ConfigSource.default.at("recommender-service").loadOrThrow[AppConfig])

}

