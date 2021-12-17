import org.scalajs.dom.html

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Thenable.Implicits._
import scala.scalajs.js.annotation.JSExportTopLevel

object App {

  import org.scalajs.dom
  import org.scalajs.dom.document

  def addText(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }

  @JSExportTopLevel("loadData")
  def loadData(pre: html.Pre): Unit = {
    addText(document.body, "Loading")
    val url = "http://localhost:9001/pipelines"
    for {
      response <- dom.fetch(url)
      text <- response.text()
    } Console.println(text)
  }


  def main(args: Array[String]): Unit = {
    addText(document.body, "pipelines")
  }
}
