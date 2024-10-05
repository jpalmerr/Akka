package AkkaClassicEssentials.actors


import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActors extends App {

  // Actors can create other actors -> use context. rather than system.

  object Parent {
    case class CreateChild(name: String)

    case class TellChild(message: String)
  }

  class Parent extends Actor {

    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        // create a new actor right HERE
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }

  import Parent._

  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child")
  parent ! TellChild("hey Kid!")

  // actor hierarchies
  // parent -> child -> grandChild
  //        -> child2 ->

  // so who owns parent ?
  /*
    Guardian actors (top-level)
    - /system = system guardian
    - /user = user-level guardian
    - / = the root guardian
   */

  /**
   * Actor selection
   */
  val childSelection = system.actorSelection("/user/parent/child")
  val badchildSelection = system.actorSelection("/user/parent/child2")
  childSelection ! "I found you!"
  badchildSelection ! "I found you!" // [akka://ParentChildDemo/user/parent/child2] was not delivered. [1] dead letters encountered

  /**
   * Danger!
   *
   * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS` REFERENCE, TO CHILD ACTORS.
   *
   * NEVER IN YOUR LIFE.
   */
}