package code.snippet

import net.liftweb.common.Loggable
import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.{JE, JsCmd}
import scala.xml.NodeSeq
import net.liftweb.util.Helpers._

object AddCounter extends Loggable {

  def render = SHtml.ajaxButton("Add Side Chat", () => addDynamicComet(), ("class" -> "btn btn-danger")  )

  def addDynamicComet(): JsCmd = {
    val html = S.runTemplate("side-chat":: Nil)
    //logger.info(html)
    val cometId = html.map{ h =>
      h \ "div" \ "@id"
    }

    val cleanedHtmlAsString = (html.openOr( NodeSeq.Empty )).toString().encJs
    val addChatbox = """$("#side-chat").html(%s)""".format( cleanedHtmlAsString )

    JE.JsRaw("""lift_toWatch['%s'] = '%s'""".format( cometId.getOrElse("default-id"), nextNum ) ).cmd &
      JE.JsRaw("""console.log(lift_toWatch)""").cmd &
      JE.JsRaw(addChatbox).cmd

  }

}
