package com.rhdzmota.crawler.model

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import scala.util.Try

case class Url(_id: UUID, uri: String, depth: Int, from: UUID, crawlRequestId: UUID, timestamp: Timestamp) {
  def toMap: Map[String, String] = Map(
    Url.Labels.ID               -> _id.toString,
    Url.Labels.URI              -> uri,
    Url.Labels.DEPTH            -> depth.toString,
    Url.Labels.FROM             -> from.toString,
    Url.Labels.CRAWL_REQUEST_ID -> crawlRequestId.toString,
    Url.Labels.TIMESTAMP        -> timestamp.toString
  )
}

object Url {

  implicit class StringOperations(str: String) {
    def toUUID: UUID = UUID.fromString(str)
    def toTimestamp: Timestamp = Timestamp.valueOf(str)
    def asInt: Option[Int] = Try(str.toInt).toOption

  }

  object Labels {
    val ID                = "_id"
    val URI               = "uri"
    val DEPTH             = "depth"
    val FROM              = "from"
    val CRAWL_REQUEST_ID  = "crawlRequestId"
    val TIMESTAMP         = "timestamp"
  }

  def apply(uri: String, depth: Int, from: UUID, crawlRequestId: UUID): Url =
    Url(UUID.randomUUID(), uri, depth, from, crawlRequestId, Timestamp.valueOf(LocalDateTime.now()))

  def crawlRequest(uri: String): Url = {
    val crawlRequestId = UUID.randomUUID()
    Url(UUID.randomUUID(), uri, 0, crawlRequestId, crawlRequestId, Timestamp.valueOf(LocalDateTime.now()))
  }

  def fromMap(map: Map[String, String]): Option[Url] = for {
    id            <- map.get(Labels.ID).map(_.toUUID)
    uri           <- map.get(Labels.URI)
    depth         <- map.get(Labels.DEPTH).flatMap(_.asInt)
    from          <- map.get(Labels.FROM).map(_.toUUID)
    crawlRequest  <- map.get(Labels.CRAWL_REQUEST_ID).map(_.toUUID)
    timestamp     <- map.get(Labels.TIMESTAMP).map(_.toTimestamp)
  } yield Url(id, uri, depth, from, crawlRequest, timestamp)

}
