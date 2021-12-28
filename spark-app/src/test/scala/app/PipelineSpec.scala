package app

import app.Util._
import cats.effect.{ IO, Timer }
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class PipelineSpec extends AnyFunSuite {

  private implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  private val rootPath = "/Users/user/data/data-sources"

  private val pipelineJobA = PipelineSimpleJob(
    "[team A] job-1",
    List(s"$rootPath/source-a" -> s"$rootPath/source-a1")
  )

  private val pipelineJobFailedA = PipelineSimpleJob(
    "[team A] job-1",
    List(s"$rootPath/source-a" -> s"$rootPath/source-a1"),
    isFailed = true
  )

  private val pipelineJobB = PipelineSimpleJob(
    "[team A] job-2",
    List(s"$rootPath/source-a1" -> s"$rootPath/source-a2")
  )

  private val pipelineJobC = PipelineSimpleJob(
    "[team A] job-3",
    List(s"$rootPath/source-a2" -> s"$rootPath/source-a3")
  )

  private val pipelineJobD = PipelineSimpleJob(
    "[team B] job-1",
    List(s"$rootPath/source-b" -> s"$rootPath/source-b1")
  )

  private val pipelineJobE = PipelineSimpleJob(
    "[team B] job-2",
    List(s"$rootPath/source-b1" -> s"$rootPath/source-b2", s"$rootPath/source-a2" -> s"$rootPath/source-ab")
  )

  private val pipelineJobF = PipelineSimpleJob(
    "[team B] job-3",
    List(s"$rootPath/source-b2" -> s"$rootPath/source-b3")
  )

  test("run pipeline A") {

    val pipelineTeamA = for {
      _ <- pipelineJobA.run
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobB.run
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobC.run
    } yield ()

    runSafe(pipelineTeamA)
  }

  test("run pipeline B") {
    val pipelineTeamB = for {
      _ <- pipelineJobD.run
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobE.run
      _ <- IO.sleep(1.seconds)
      _ <- pipelineJobF.run
    } yield ()

    runSafe(pipelineTeamB)

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
