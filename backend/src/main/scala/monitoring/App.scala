package monitoring

import cats.effect.{ExitCode, IO, IOApp}
import doobie.Transactor
import fs2.concurrent.Topic
import model.Events.PipelineInfo
import monitoring.config.AppConfig
import monitoring.modules.{HttpApi, Services}
import monitoring.resources.AppResources
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object App extends IOApp {

  private def httpApp(postgres: Transactor[IO]): IO[HttpApp[IO]] = {
    for {
      eventTopic <- Topic[IO, List[PipelineInfo]](initial = Nil)
      services <- IO(Services.make[IO](postgres))
    } yield HttpApi.make[IO](services).httpApp(eventTopic)
  }


  override def run(args: List[String]): IO[ExitCode] = {
    val config = AppConfig.default

    AppResources.make[IO](config).use(appResources =>
      for {
        httpApp <- httpApp(appResources.postgres)
        _ <- BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = 9001, host = "localhost")
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
      } yield ExitCode.Success
    )
  }

}
