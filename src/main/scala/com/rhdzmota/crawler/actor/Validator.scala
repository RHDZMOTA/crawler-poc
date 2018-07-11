package com.rhdzmota.crawler.actor

import java.sql.Timestamp

import akka.actor.Actor
import com.rhdzmota.crawler.Settings
import com.rhdzmota.crawler.model.Url

class Validator extends Actor {

  var seenUrls: Map[String, Timestamp] = Map[String, Timestamp]()

  def updateUrls(uri: String, timestamp: Timestamp): Unit =
    seenUrls + (uri -> timestamp)

  override def receive: Receive = {
    case _ @ Url(_, uri, _, _, _, _, timestamp) =>
      val valid: Boolean = seenUrls.get(uri)
        .map(ts => (timestamp.getNanos - ts.getNanos) < Settings.Crawler.urlLifeSpan.toNanos)
        .getOrElse(true)
      if (valid) updateUrls(uri, timestamp)
      sender() ! valid
  }
}
