package com.rhdzmota.crawler.service.pubsub

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import com.rhdzmota.crawler.model.Url

trait PubSub[Message, AckReq, AckResp, PubResp] {
  def source: Source[Message, NotUsed]
  def ackSink: Sink[AckReq, AckResp]
  def sink: Sink[Url, PubResp]
  def publish(url: Url): PubResp
}
