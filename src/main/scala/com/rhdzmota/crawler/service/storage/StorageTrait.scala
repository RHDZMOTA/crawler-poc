package com.rhdzmota.crawler.service.storage

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}

trait StorageTrait[Converter, ContentType, BlobType] {
  def uploadFile(fileName: String, content: ContentType): BlobType
  def toFile: Flow[Converter, (String, ContentType), NotUsed]
  def sink: Sink[(String, ContentType), NotUsed]
}
