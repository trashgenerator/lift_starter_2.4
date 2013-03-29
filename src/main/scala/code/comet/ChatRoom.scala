package code.comet

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.ClearClearable
import xml.NodeSeq
import js.{JE, JsCmd}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime
import net.liftweb.json.ext.JodaTimeSerializers
import code.snippet.CurrentUser
import net.liftweb.actor.LAFuture


class ChatRoom extends NamedCometActorTrait with Loggable {

  override def lowPriority = {
    case message: ChatMessage =>
      partialUpdate( SingleChatMessage( message ) )

    case InitialRender =>
      logger.info("Initial render")
      val futureStoredMessages = Storage !< GetAll
      sendStoredMessages( futureStoredMessages )


    case x => logger.info("got: %s as a message " format x)

  }

  def render = {
    "#message-bind  [data-ng-bind]"       #> "todo.message" &
    "#timestamp     [data-ng-bind]"       #> "todo.createdAt" &
    "#username      [data-ng-bind]"       #> "todo.username" &
    "#message-id    [data-ng-bind]"       #> "todo.id" &
    "#messages-repeater [data-ng-repeat]" #> "todo in todos" &
    ClearClearable
  }

  override def fixedRender: Box[NodeSeq] = {
    this ! InitialRender
    logger.info("Calling Initial render")
    NodeSeq.Empty
  }

  private[this] def sendStoredMessages(futureStoredMessages: LAFuture[Any]) {
    futureStoredMessages.foreach { storedMessages =>
      for {
        maybeMessages  <- Box.asA[Option[InboxMessages]](storedMessages)
        messages       <- maybeMessages
      } yield {
        logger.info("made it %s" format messages)
        partialUpdate( StoredChatMessage( messages ) )
      }
    }
  }



}


case object InitialRender

case class ChatMessage(id: String, username: String, message: String, createdAt: DateTime) {
  def toJavaScript: String = {
    """{id}"""
  }
}
case class SingleChatMessage(data: ChatMessage) extends ChatRoomEvent("new-chat-message")
case class StoredChatMessage(data: InboxMessages) extends ChatRoomEvent("initial-chat-messages")

class ChatRoomEvent(event: String) extends JsCmd {
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
  val json = Extraction.decompose(this)
  override def toJsCmd = JE.JsRaw("""$(document).trigger('%s', %s)""".format(event,  compact( render( json ) ) ) ).cmd.toJsCmd
}

trait JavaScriptObject[T] {
  def toJavaScript(value: T): String
}


