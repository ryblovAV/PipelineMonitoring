package monitoring.services

import model.PipelineEventType
import monitoring.db.Domain._

import scala.annotation.tailrec

object DependencyChecker {

  object PipelineJobState extends Enumeration {
    val running, success, failed, outdated, waiting = Value
  }

  case class PipelineJobWithInputs(
      id: PipelineJobId,
      name: String,
      eventType: PipelineEventType.Value,
      inputs: List[PipelineJobWithInputs]
  )

  @tailrec
  private def resolveInputDependency(
      pipelineJobs: List[PipelineJob],
      inputs: Map[PipelineJobId, List[DataSourceId]],
      outputs: Map[DataSourceId, PipelineJobId],
      acc: Map[PipelineJobId, PipelineJobWithInputs]
  ): List[PipelineJobWithInputs] = {

    val (notProcessed, processed) =
      pipelineJobs
        .map { pipelineJob =>
          inputs.get(pipelineJob.pipelineJobId) match {
            case Some(inputDataSourceIds) =>
              val inputPipelineJobs = for {
                inputDataSourceId  <- inputDataSourceIds
                inputPipelineJobId <- outputs.get(inputDataSourceId)
              } yield inputPipelineJobId

              if (inputPipelineJobs.forall(acc.contains)) {
                Right(
                  PipelineJobWithInputs(
                    pipelineJob.pipelineJobId,
                    pipelineJob.name,
                    pipelineJob.eventType,
                    inputPipelineJobs.flatMap(acc.get)
                  )
                )
              } else Left(pipelineJob)

            case None =>
              Right(
                PipelineJobWithInputs(
                  pipelineJob.pipelineJobId,
                  pipelineJob.name,
                  pipelineJob.eventType,
                  Nil
                )
              )
          }
        }
        .partitionMap(identity)

    if (processed.isEmpty || notProcessed.isEmpty) {
      acc.values.toList ++ processed
    } else {
      resolveInputDependency(notProcessed, inputs, outputs, acc ++ processed.map(p => p.id -> p))
    }
  }

  @tailrec
  private def calcState(
      pipelines: List[PipelineJobWithInputs],
      acc: Map[PipelineJobId, (PipelineJobWithInputs, PipelineJobState.Value)] = Map.empty
  ): List[(PipelineJobWithInputs, PipelineJobState.Value)] = {

    def isSuccess(pipelineJob: PipelineJobWithInputs): Boolean =
      pipelineJob.eventType == PipelineEventType.finish &&
        pipelineJob.inputs.forall(
          input => acc.get(input.id).exists { case (_, state) => state == PipelineJobState.success }
        )

    def isFailed(pipelineJob: PipelineJobWithInputs): Boolean =
      pipelineJob.eventType == PipelineEventType.failed

    def isRunning(pipelineJob: PipelineJobWithInputs): Boolean =
      pipelineJob.eventType == PipelineEventType.start

    def isOutdated(pipelineJob: PipelineJobWithInputs): Boolean =
      pipelineJob.eventType == PipelineEventType.finish &&
        pipelineJob.inputs.exists(
          input =>
            acc.get(input.id).exists {
              case (_, state) =>
                state == PipelineJobState.failed || state == PipelineJobState.outdated
            }
        )

    val (notCalculated, calculated) = pipelines
      .map {
        case p if isFailed(p)   => Right(p, PipelineJobState.failed)
        case p if isRunning(p)  => Right(p, PipelineJobState.running)
        case p if isSuccess(p)  => Right(p, PipelineJobState.success)
        case p if isOutdated(p) => Right(p, PipelineJobState.outdated)
        case p                  => Left(p)
      }
      .partitionMap(identity)

    if (calculated.isEmpty || notCalculated.isEmpty) {
      acc.values.toList ++
        calculated ++
        notCalculated.map { p =>
          p -> PipelineJobState.waiting
        }
    } else {
      calcState(notCalculated, acc ++ calculated.map { case (p, state) => p.id -> (p, state) })
    }
  }

  def run(
      pipelineJobs: List[PipelineJob],
      inputDataSources: List[DataSourceConnection],
      outputDataSources: List[DataSourceConnection]
  ): List[(PipelineJobWithInputs, PipelineJobState.Value)] = {

    val inputs = inputDataSources
      .groupBy(_.pipelineJobId)
      .map {
        case (pipelineJobId, dataSources) =>
          pipelineJobId -> dataSources.map(_.dataSourceId)
      }

    val outputs = outputDataSources
      .groupBy(_.dataSourceId)
      .map {
        case (dataSourceId, dataSources) =>
          dataSourceId -> dataSources.map(_.pipelineJobId).head
      }

    calcState(
      resolveInputDependency(pipelineJobs, inputs, outputs, Map())
    )
  }

}
