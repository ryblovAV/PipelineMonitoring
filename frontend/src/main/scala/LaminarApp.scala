import com.raquo.laminar.api.L._
import org.scalajs.dom

object LaminarApp {

  val nameVar = Var(initial = "world")

  val rootElement = div(
    table(
      tr(
        td(1), td(2), td(3)
      ),
      tr(
        td(1), td(2), td(3)
      )
    ),

    label("Your name: "),
    input(
      onMountFocus,
      placeholder := "Enter your name here",
      inContext { thisNode => onInput.map(_ => thisNode.ref.value) --> nameVar }
    ),
    span(
      "Hello, ",
      child.text <-- nameVar.signal.map(_.toUpperCase)
    )
  )



  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach { _ =>
      val appContainer = dom.document.querySelector("#app")
      appContainer.innerHTML = "<b>test</b>"
      val _ = render(appContainer, rootElement)
    }(unsafeWindowOwner)


  }

}
