package AkkaClassicEssentials.actors

//import akka.actor.TypedActor.context
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender() ! "Hello, there!"
      case message: String => println(s"[${self.path}] I have received $message")
      case number: Int => println(s"[simple actor] I have received a number $number")
      case sm: SpecialMessage => println(s"[simple actor] I have something special $sm")
      case SendMessageToMyself(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi!"
    }
  }
  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor"

  simpleActor ! 42

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  /**
   * 1)
   * messages can be of any type
   * a) messages must be IMMUTABLE
   * b) messages must be SERIALIZABLE
   * in practise use case classes & case objects
   */

  // 2)
  // actors have information about their context and themselves
  // context.self === this in OOP

  case class SendMessageToMyself(content: String)
  simpleActor ! SendMessageToMyself("hello me")

  // 3)
  // actors can REPLY to messages

  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob) // [akka://actorCapabilitiesDemo/user/bob] I have received Hi!, [akka://actorCapabilitiesDemo/user/alice] I have received Hello, there!

  /**
   * notice
   * def !(message: Any)(implicit sender: ActorRef = Actor.noSender)
   * send themselves as sender
   * can miss as self is implicit value -> can override this
   */1

}
