package app

import app.Util._
import cats.effect.{ IO, Timer }
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class PipelineSpec extends AnyFunSuite {

  private implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  private val rootPath = "/Users/user/data/data-sources"

  val inputPath = s"$rootPath/source-a"

  private val pipelineJobA = PipelineSimpleJob(
    "pipeline-job-A",
    List(s"$rootPath/source-a" -> s"$rootPath/source-a1", s"$rootPath/source-b" -> s"$rootPath/source-b1")
  )

  private val pipelineJobFailedA = PipelineSimpleJob(
    "pipeline-job-A",
    List(s"$rootPath/source-a" -> s"$rootPath/source-a1", s"$rootPath/source-b" -> s"$rootPath/source-b1"),
    isFailed = true
  )

  private val pipelineJobB = PipelineSimpleJob(
    "pipeline-job-B",
    List(s"$rootPath/source-a1" -> s"$rootPath/source-a2", s"$rootPath/source-b1" -> s"$rootPath/source-b2")
  )

  private val pipelineJobC = PipelineSimpleJob(
    "pipeline-job-C",
    List(s"$rootPath/source-a1" -> s"$rootPath/source-a3", s"$rootPath/source-b2" -> s"$rootPath/source-b3")
  )

  private val pipelineJobD = PipelineSimpleJob(
    "pipeline-job-D",
    List(s"$rootPath/source-a2" -> s"$rootPath/source-a4")
  )

  test("run pipeline") {

    val pipeline = for {
      _ <- pipelineJobA.run
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobB.run
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobC.run
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobD.run
    } yield ()

    runSafe(pipeline)
  }

  test("run failed pipeline job A") {
    val pipeline = for {
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobFailedA.run
    } yield ()

    runSafe(pipeline)
  }

  test("run success pipeline job A") {
    val pipeline = for {
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobA.run
    } yield ()

    runSafe(pipeline)
  }

}
