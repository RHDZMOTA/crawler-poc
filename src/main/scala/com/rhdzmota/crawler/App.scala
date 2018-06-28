package com.rhdzmota.crawler

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.alpakka.googlecloud.pubsub.ReceivedMessage
import akka.stream.scaladsl.{Broadcast, GraphDSL, RunnableGraph}
import com.rhdzmota.crawler.model.Url
import com.rhdzmota.crawler.service.database.impl.Cassandra
import com.rhdzmota.crawler.service.pubsub.impl.GCPubSub
import com.rhdzmota.crawler.service.storage.impl.GCStorage

object App extends Context {

  val runnable: RunnableGraph[NotUsed] = RunnableGraph.fromGraph {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Broadcasting Variables
      val bcastReceivedMessages = builder.add(Broadcast[ReceivedMessage](outputPorts = 2))
      val bcastDownload         = builder.add(Broadcast[(Url, Option[String])](outputPorts = 4))

      // Runnable Graph Definition
      GCPubSub.source ~>  bcastReceivedMessages ~> Crawler.validateAndDownload  ~>  bcastDownload ~> Crawler.extractUrls        ~> GCPubSub.sink
                          bcastReceivedMessages ~> GCPubSub.ackRequest          ~>  GCPubSub.ackSink
                                                                                    bcastDownload ~> Cassandra.connectorToSink  ~> Cassandra.sink
                                                                                    bcastDownload ~> GCStorage.toFile           ~> GCStorage.sink
                                                                                    bcastDownload ~> Crawler.printSink
      ClosedShape
    }
  }

  def main(args: Array[String]): Unit = {
    runnable.run()
  }

}
