package AkkaClassicEssentials.actors

import akka.actor.{Actor, ActorSystem, Props}

object Actors extends App {

  // akka system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // actor
  class WordCountActor extends Actor {
    // state
    var totalWords = 0

    // behavior
    def receive: Receive = { // Receive = PartialFunction[Any, Unit]
      case message: String =>
        totalWords += message.split(" ").length
        println(s"Current word count: $totalWords")
      case msg => println(s"[word counter] I cannot understand $msg")
    }
  }

  // instantiate
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // communicate
  wordCounter ! "I am learning Akka"
  anotherWordCounter ! "different message"

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }
  // encouraged
  object Person {
    def props(name: String) = Props(new Person(name))
  }

//  val person = actorSystem.actorOf(Props(new Person("Bob"))) // discouraged
  val person = actorSystem.actorOf(Person.props("Bob")) // encouraged
  person ! "hi"



}
