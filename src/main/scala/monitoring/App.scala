package monitoring

import cats.effect.{ExitCode, IO, IOApp}
import monitoring.modules.{HttpApi, Services}
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object App extends IOApp {

  private val httpApp: IO[HttpApp[IO]] = IO {
    val services: Services[IO] = Services.make[IO]
    HttpApi.make[IO](services).httpApp
  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      httpApp <- httpApp
      _ <- BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(port = 9001, host = "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    } yield ExitCode.Success
  }

}
