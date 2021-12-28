package monitoring

import cats.effect.{ ExitCode, IO, IOApp }
import cats.syntax.all._
import constants.KafkaConstants.PIPELINE_JOB_TOPIC_NAME
import fs2.concurrent.Topic
import fs2.kafka._
import model.PipelineInfo
import monitoring.config.{ AppConfig, HttpConfig }
import monitoring.modules.{ EventProcessor, HttpApi, Services }
import monitoring.resources.AppResources
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object App extends IOApp {

  private def startHttpServer(config: HttpConfig, services: Services[IO]): IO[Unit] =
    for {
      httpApp <- IO(HttpApi.make[IO](services).httpApp)
      _ <- BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(port = config.port, host = config.host)
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    } yield ()

  private def processEventsApp(consumer: KafkaConsumer[IO, String, String], services: Services[IO]): IO[Unit] = {
    val eventProcessor = EventProcessor.make(services)
    consumer.subscribeTo(PIPELINE_JOB_TOPIC_NAME) >> consumer.stream
      .evalMap { msg =>
        eventProcessor.processRecord(msg).as(msg.offset)
      }
      .through(commitBatchWithin(100, 15.seconds))
      .compile
      .drain
  }

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO use https://pureconfig.github.io
    val config = AppConfig.default

    AppResources
      .make[IO](config)
      .use(
        appResources =>
          for {
            eventTopic <- Topic[IO, List[PipelineInfo]](initial = Nil)
            services   <- IO(Services.make[IO](appResources.postgres, eventTopic))
            _ <- (
              startHttpServer(config.http, services),
              processEventsApp(appResources.kafka, services)
            ).parMapN((_, _) => ())
          } yield ExitCode.Success
      )
  }

}
