package monitoring.services

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.functor._
import monitoring.domain.PipelineJob

trait Pipelines[F[_]] {
  def all: F[List[PipelineJob]]
}

object Pipelines {
  def make[F[_]: Applicative]: Pipelines[F] = {
    new Pipelines[F] {
      override def all: F[List[PipelineJob]] = {
        List(
          PipelineJob("A", Nil, Nil),
          PipelineJob("B", Nil, Nil, isFailed = true)
        ).pure
      }
    }
  }
}