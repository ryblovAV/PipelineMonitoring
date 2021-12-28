package monitoring.config

case class DBConfig(
    driverName: String,
    url: String,
    user: String,
    password: String
)

case class HttpConfig(port: Int, host: String)

case class KafkaConfig(bootstrapServers: String, groupId: String)

case class AppConfig(http: HttpConfig, db: DBConfig, kafka: KafkaConfig)

object AppConfig {

  val default: AppConfig = AppConfig(
    HttpConfig(
      port = 9001,
      host = "localhost"
    ),
    DBConfig(
      driverName = "org.postgresql.Driver",
      url = "jdbc:postgresql:job_monitoring",
      user = "docker",
      password = "docker"
    ),
    KafkaConfig(
      bootstrapServers = "localhost:9092",
      groupId = "pipeline"
    )
  )
}
