package AkkaClassicEssentials.actors

import AkkaClassicEssentials.actors.ChangingActorBehavior.Mum.MumStart
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val happy = "happy"
    val sad = "sad"
  }

  class FussyKid extends Actor {
    import AkkaClassicEssentials.actors.ChangingActorBehavior.FussyKid._
    import AkkaClassicEssentials.actors.ChangingActorBehavior.Mum._
    var state = happy
    override def receive: Receive = {
      case Food(VEGETABLE) => state = sad
      case Food(CHOCOLATE) => state = happy
      case Ask(message) =>
        if (state == happy) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  object Mum {
    case class MumStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String) // question
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mum extends Actor {
    import ChangingActorBehavior.FussyKid._
    import ChangingActorBehavior.Mum._
    override def receive: Receive = {
      case MumStart(kidRef) =>
        println("started")
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
//        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play")
      case KidAccept => println("yay kid is happy")
      case KidReject => println("noo kid is sad")
    }
  }

  val system = ActorSystem("changingActorBehavior")
  val fussyKid = system.actorOf(Props[FussyKid])
  val mum = system.actorOf(Props[Mum])

//  mum ! MumStart(fussyKid)

  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  mum ! MumStart(statelessFussyKid)

  // what about a stateless fussy kid

  class StatelessFussyKid extends Actor {
    import ChangingActorBehavior.FussyKid._
    import ChangingActorBehavior.Mum._
    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)  // change handler to sad receive
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false) // stack sadReceives => even sadder
//      case Food(CHOCOLATE) => context.become(happyReceive, false) // change handler to happy receive
      case Food(CHOCOLATE) => context.unbecome
      case Ask(_) => sender() ! KidReject
    }

    /**
     * false => stacks handlers
     *
     * Food(veg) -> stack.push(sadReceive)
     * Food(chocolate) -> stack.push(happyReceive)
     *
     * Stack
     * 1. happyReceive
     * 2. sadReceive
     * 3. happyReceive
     */

    /**
     * Food(veg)
     * Food(veg)
     * Food(chocolate)
     *
     * ->
     * Stack:
     * 1. sadReceive
     * 2. sadReceive
     * 3. happyReceive
     *
     * -> .unbecome (pops top from stack and drops to next up)
     * 1. sadReceive
     * 2. happyReceive
     *
     * Food(chocolate)
     * 1. happyReceive
     */
  }
}
