package AkkaStreams.graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanInShape2, UniformFanOutShape}
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip}

object GraphBasics extends App {

  implicit val system = ActorSystem("GraphBasics")
  // this line needs to be here for Akka < 2.6
  // implicit val materializer: ActorMaterializer = ActorMaterializer()

  val input = Source(1 to 1000)
  val incrementer = Flow[Int].map(x => x + 1) // hard computation
  val multiplier = Flow[Int].map(x => x * 10) // hard computation
  val output = Sink.foreach[(Int, Int)](println)

  // fan in -> multiple streams into one graph
  // then fan out -> one graph into multiple outputs

  // step 1 - setting up the fundamentals for the graph
  val graph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] => // builder = MUTABLE data structure
      import GraphDSL.Implicits._ // brings some nice operators into scope

      // step 2 - add the necessary components of this graph
      val broadcast: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2)) // fan-out operator (single input, 2 outputs)
      val zip: FanInShape2[Int, Int, (Int, Int)] = builder.add(Zip[Int, Int]) // fan-in operator (2 inouts one output)

      // step 3 - tying up the components
      input ~> broadcast // input feeds into broadcast

      broadcast.out(0) ~> incrementer ~> zip.in0
      broadcast.out(1) ~> multiplier  ~> zip.in1

      zip.out ~> output

      // step 4 - return a closed shape
      ClosedShape // FREEZE the builder's shape
      // shape
    } // graph
  ) // runnable graph

  //  graph.run() // run the graph and materialize it

  /**
   * exercise 1: feed a source into 2 sinks at the same time (hint: use a broadcast)
   */

  val firstSink = Sink.foreach[Int](x => println(s"First sink: $x"))
  val secondSink = Sink.foreach[Int](x => println(s"Second sink: $x"))

  // step 1
  val sourceToTwoSinksGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // step 2 - declaring the components
      val broadcast = builder.add(Broadcast[Int](2))

      // step 3 - tying up the components
      input ~>  broadcast ~> firstSink  // implicit port numbering
      broadcast ~> secondSink
      //      broadcast.out(0) ~> firstSink
      //      broadcast.out(1) ~> secondSink

      // step 4
      ClosedShape
    }
  )

  /**
   * exercise 2: balance
   */

  import scala.concurrent.duration._
  // Throttle the input streams to control the rate at which elements are processed.
  // 'fastSource' emits 5 elements per second, while 'slowSource' emits 2 elements per second
  val fastSource = input.throttle(5, 1.second)
  val slowSource = input.throttle(2, 1.second)

  val sink1 = Sink.fold[Int, Int](0)((count, _) => {
    println(s"Sink 1 number of elements: $count")
    count + 1
  })

  val sink2 = Sink.fold[Int, Int](0)((count, _) => {
    println(s"Sink 2 number of elements: $count")
    count + 1
  })

  // step 1
  val balanceGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._


      // step 2 -- declare components
      /**
       * Merge: Combines both input streams (fastSource and slowSource) into one.
       * Balance: Distributes the merged stream evenly between two sinks (sink1 and sink2)
       */
      val merge = builder.add(Merge[Int](2))
      val balance = builder.add(Balance[Int](2))


      // step 3 -- tie them up
      fastSource ~> merge ~>  balance ~> sink1
      slowSource ~> merge
      balance ~> sink2

      // step 4
      ClosedShape
    }
  )

  balanceGraph.run()


}
