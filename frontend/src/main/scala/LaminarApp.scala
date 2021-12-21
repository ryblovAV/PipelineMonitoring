import com.raquo.laminar.CollectionCommand
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import io.laminext.websocket._
import org.scalajs.dom.html

import com.raquo.laminar.api.L._
import com.raquo.laminar.CollectionCommand

import io.circe._


object LaminarApp {

  private val ws = WebSocket.url("ws://localhost:9001/pipelines/info").string.build(managed = true, bufferSize = Int.MaxValue)

  val nameVar = Var(initial = "world")

  val rootElement: ReactiveHtmlElement[html.Div] = div(
    div(
      ws.connect
    ),
    div(
      cls := "flex-1",
      div(
        code("pipelines:")
      ),
      div(
        cls := "flex flex-col space-y-4 p-4 max-h-48 overflow-auto bg-gray-900 text-green-400 text-xs",
        children.command <-- ws.received.map { message =>
          println(message)
          CollectionCommand.Append(
            message
          )
        }
      )
    )
  )

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach { _ =>
      val appContainer = dom.document.querySelector("#app")
      val _ = render(appContainer, rootElement)
    }(unsafeWindowOwner)
  }

}
