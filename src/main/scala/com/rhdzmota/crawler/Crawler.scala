package com.rhdzmota.crawler

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.stream.alpakka.googlecloud.pubsub.ReceivedMessage
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.Timeout
import com.rhdzmota.crawler.actor.Validator
import com.rhdzmota.crawler.model.Url
import com.rhdzmota.crawler.util.ClientHttp
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

object Crawler extends ClientHttp with Context {

  implicit val askTimeout: Timeout = Timeout(Settings.Crawler.askTimeout)

  val validator: ActorRef = actorSystem.actorOf(Props[Validator])

  val toUrl: Flow[ReceivedMessage, Option[Url], NotUsed] =
    Flow[ReceivedMessage].map(
      receivedMessage => receivedMessage.message.attributes.flatMap(Url.fromMap))

  val recentCrawlValidation: Flow[Url, Url, NotUsed] =
    Flow[Url].mapAsync(Settings.Crawler.parallelism)(url => (validator ? url).mapTo[Boolean].map(x => (url, x)))
      .filter(_._2).map(_._1)

  val depthValidation: Flow[Option[Url], Url, NotUsed] =
    Flow[Option[Url]].filter {
      case Some(url) => url.depth <= Settings.Crawler.depth
      case None => false
    } map {_.get}

  val validate: Flow[Option[Url], Url, NotUsed] =
    depthValidation via recentCrawlValidation

  val download: Flow[Url, (Url, Option[String]), NotUsed] =
    Flow[Url].mapAsync[(Url, Option[String])](Settings.Crawler.parallelism)(getRequestAsString)

  val validateAndDownload: Flow[ReceivedMessage, (Url, Option[String]), NotUsed] =
    toUrl via validate via download

  val printSink: Sink[(Url, Option[String]), NotUsed] =
    Flow[(Url, Option[String])].map({ case (x, y) => x }).to(Sink.foreach(println))

  val extractUrls: Flow[(Url, Option[String]), Url, NotUsed] =
    Flow[(Url, Option[String])].mapConcat(from => getUrls(from))

  def getUrls(from: (Url, Option[String])): List[Url] = from match {
    case (_, None) => Nil
    case (url, Some(response)) => Jsoup.parse(response).select("a[href]").asScala.toList
      .filter(_.attr("href").length != 0)
      .map(
      element => {
        val href = element.attr("href").trim
        val depth = url.depth + 1
        if (href.startsWith("https")) Url(href, depth, url._id, url.crawlRequestId)
        else Url(url.uri + href, depth, url._id, url.crawlRequestId)
      }
    )
  }
}

