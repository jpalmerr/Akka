- Publisher: emits elements asynchronously
- Subscriber: receives elements
- Processor: transforms elements along the way

Reactive Streams is an SPI not API 
- our focus is the Akka Streams API



### Akka Streams

- Source = "publisher"
- Sink = "subscriber"
- Flow = "processor"

upstream = to the source
downstream = to the sink


### Materialized Values

components are static until you trigger .run
`val res = graph.run() // = materializing`

Materializing a graph = materializing all components
- each component produces a materialized value when run
- the graph produces a single materialized value
- our job is to choose which one to pick

A component can materialise multiple times
- you can reuse the same component in different graphs
- different runs = different materialization's 

A materialized value can be ANYTHING