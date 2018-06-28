package com.rhdzmota.crawler.service.database

import akka.NotUsed
import akka.stream.scaladsl._
import com.rhdzmota.crawler.model._

trait Database[Connector, SinkResult] {
  def connectorToSink: Flow[Connector, Url, NotUsed]
  def sink: Sink[Url, SinkResult]
}
