package AkkaTypedEssentials.actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors


object ActorState {
  /*
  Exercise: use the setup method to create a word counter which
    - splits each message into words
    - keeps track of the TOTAL number of words received so far
    - log the current # of words + TOTAL # of words
   */

  object WordCounter {
    def apply(): Behavior[String] = Behaviors.setup { context =>
      var total = 0

      // first message behaviour
      Behaviors.receiveMessage { message =>
        val newCount = message.split(" ").length
        total += newCount
        context.log.info(s"Message word count: $newCount - total count $total")
        Behaviors.same
      }
    }
  }

  sealed trait SimpleThing
  case object EatChocolate extends SimpleThing
  case object CleanTheFloor extends SimpleThing
  case object LearnAkkA extends SimpleThing

  /*
    message types must be IMMUTABLE and SERIALIZABLE
   */

  object SimpleHuman {
    def apply(): Behavior[SimpleThing] = Behaviors.setup { context =>
      var happiness = 0

      Behaviors.receiveMessage {
        case EatChocolate =>
          context.log.info(s"[$happiness] eating chocolate")
          happiness += 1
          Behaviors.same
        case CleanTheFloor =>
          context.log.info(s"[$happiness] Wiping the floor")
          happiness -= 2
          Behaviors.same
        case LearnAkkA =>
          context.log.info(s"[$happiness] learning akka woo")
          happiness += 10
          Behaviors.same
      }
    }
  }

  def demoSimpleHuman(): Unit = {
    val human = ActorSystem(SimpleHuman(), "DemoSimpleHuman")

    human ! LearnAkkA
    human ! EatChocolate
    ((1 to 10)).foreach(_  => human ! CleanTheFloor)

    Thread.sleep(1000)
    human.terminate() // factory method
  }

  def demoSimpleHuman2(): Unit = {
    val human = ActorSystem(SimpleStatelessHuman(), "DemoSimpleHuman")

    human ! LearnAkkA
    human ! EatChocolate
    ((1 to 10)).foreach(_  => human ! CleanTheFloor)

    Thread.sleep(1000)
    human.terminate() // factory method
  }



  // lets refactor out the mutability

  object SimpleStatelessHuman {
    def apply(): Behavior[SimpleThing] = statelessHuman(0)

    def statelessHuman(happiness: Int): Behavior[SimpleThing] = Behaviors.receive { (context, message) =>
      message match {
        case CleanTheFloor =>
          context.log.info(s"[$happiness] eating chocolate")
          statelessHuman(happiness + 1) // not quire recursion - won't necessarily be the same thread working on next one
        case EatChocolate =>
          context.log.info(s"[$happiness] Wiping the floor")
          statelessHuman(happiness - 2)
        case LearnAkkA =>
          context.log.info(s"[$happiness] learning akka woo")
          statelessHuman(happiness + 10)
      }
    }
  }

  object WordCounter_V2 {
    def apply(): Behavior[String] = active(0)

    def active(total: Int): Behavior[String] = Behaviors.setup { context =>
      Behaviors.receiveMessage { message =>
        val newCount = message.split(" ").length
        context.log.info(s"Message word count: $newCount - total count: ${total + newCount}")
        active(total + newCount)
      }
    }
  }

  def demoWordCounter(): Unit = {
    val wordCounter = ActorSystem(WordCounter_V2(), "WordCounterDemo")

    wordCounter ! "I am learning Akka"
    wordCounter ! "I hope you will be stateless one day"
    wordCounter ! "Let's see the next one"

    Thread.sleep(1000)
    wordCounter.terminate()
  }



  def main(args: Array[String]): Unit = {
//    demoSimpleHuman()
//    demoSimpleHuman2()
    demoWordCounter()
  }


}
