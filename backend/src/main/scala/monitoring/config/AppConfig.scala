package monitoring.config


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

}

