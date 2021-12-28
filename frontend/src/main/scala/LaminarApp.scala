import com.raquo.laminar.api.L._
import io.circe._
import io.laminext.websocket._
import io.laminext.websocket.circe.webSocketReceiveBuilderSyntax
import org.scalajs.dom

object LaminarApp {

  final case class PipelineStatusEvent(pipelineName: String, status: String)
  final case class ClientCommand(name: String)

  implicit val PipelineStatusEventCodec: Codec[PipelineStatusEvent] =
    Codec.from(
      Decoder.forProduct2("pipelineName", "status")(PipelineStatusEvent.apply),
      Encoder.forProduct2("pipelineName", "status")(
        e => (e.pipelineName, e.status)
      )
    )

  implicit val clientCommandCodec: Codec[ClientCommand] = Codec.from(
    Decoder.forProduct1("name")(ClientCommand.apply),
    Encoder.forProduct1("name")(_.name)
  )

  private val ws = WebSocket
    .url("ws://localhost:9001/pipelines/info")
    .json[List[PipelineStatusEvent], ClientCommand]
    .build(managed = true, bufferSize = Int.MaxValue)

  def renderPipeline(pipeline: PipelineStatusEvent): Div =
    div(
      p(s"${pipeline.pipelineName}: ${pipeline.status}")
    )

  def pipelinesListElement =
    div(
      div(
        ws.connect
      ),
      div(),
      div(
        children <-- ws.received.map(pipelines => pipelines.map(renderPipeline))
      )
    )

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach { _ =>
      val appContainer = dom.document.querySelector("#app")
      val _            = render(appContainer, pipelinesListElement)
    }(unsafeWindowOwner)
  }

}
