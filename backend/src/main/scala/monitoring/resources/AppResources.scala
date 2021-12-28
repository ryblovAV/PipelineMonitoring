package monitoring.resources

import cats.effect.{ Async, Blocker, ConcurrentEffect, ContextShift, Resource, Timer }
import doobie.hikari.HikariTransactor
import doobie.{ ExecutionContexts, Transactor }
import fs2.kafka._
import monitoring.config.{ AppConfig, DBConfig, KafkaConfig }

trait AppResources[F[_]] {
  def postgres: Transactor[F]
  def kafka: KafkaConsumer[F, String, String]
}

object AppResources {

  private def makeKafkaResources[F[_]: ConcurrentEffect: ContextShift: Timer](
      config: KafkaConfig
  ): Resource[F, KafkaConsumer[F, String, String]] = {

    val consumerSettings =
      ConsumerSettings[F, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withEnableAutoCommit(true)
        .withBootstrapServers(config.bootstrapServers)
        .withGroupId(config.groupId)

    KafkaConsumer.resource(consumerSettings)
  }

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
        blocker = be    // execute JDBC operations on this EC
      )
    } yield xa

  def make[F[_]: ContextShift: ConcurrentEffect: Timer](config: AppConfig): Resource[F, AppResources[F]] =
    for {
      transactor    <- makeDBResources(config.db)
      kafkaConsumer <- makeKafkaResources(config.kafka)
    } yield new AppResources[F] {
      val postgres: Transactor[F]                 = transactor
      val kafka: KafkaConsumer[F, String, String] = kafkaConsumer
    }
}
