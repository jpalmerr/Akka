package AkkaStreams.primer

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

object BackpressureBasics extends App {

  implicit val system = ActorSystem("BackpressureBasics")
  // this line needs to be here for Akka < 2.6
  // implicit val materializer: ActorMaterializer = ActorMaterializer()

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int] { x =>
    // simulate a long processing
    Thread.sleep(1000)
    println(s"Sink: $x")
  }

  //  fastSource.to(slowSink).run() // fusing?!
  // not backpressure

  //    fastSource.async.to(slowSink).run()
  // backpressure

  val simpleFlow = Flow[Int].map { x =>
    println(s"Incoming: $x")
    x + 1
  }

  fastSource.async
    .via(simpleFlow).async
    .to(slowSink) // slow sink -> back pressure signals upstream because its slow -> buffers internally until default buffer (16)
  //    .run()

  /*
    reactions to backpressure (in order):
    - try to slow down if possible
    - buffer elements until there's more demand
    - drop down elements from the buffer if it overflows <- we can only control at this moment
    - tear down/kill the whole stream (failure)
   */

  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  fastSource.async
    .via(bufferedFlow).async
    .to(slowSink)
//    .run()

  /*
    1-16: nobody is backpressured
    17-26: flow will buffer, flow will start dropping at the next element
    26-1000: flow will always drop the oldest element
      => 991-1000 => 992 - 1001 => sink
   */

  /*
    overflow strategies:
    - drop head = oldest
    - drop tail = newest
    - drop new = exact element to be added = keeps the buffer
    - drop the entire buffer
    - backpressure signal
    - fail
   */

  // throttling
  import scala.concurrent.duration._
  fastSource.throttle(10, 1.second).runWith(Sink.foreach(println)) // x elements per y seconds
}
