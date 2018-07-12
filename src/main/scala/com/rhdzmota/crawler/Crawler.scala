package com.rhdzmota.crawler

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.stream.alpakka.googlecloud.pubsub.ReceivedMessage
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.Timeout
import com.rhdzmota.crawler.actor.Validator
import com.rhdzmota.crawler.model.{CustomResponse, Url}
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
      case Some(url) => url.depth <= url.maxDepth
      case None => false
    } map {_.get}

  val validate: Flow[Option[Url], Url, NotUsed] =
    depthValidation via recentCrawlValidation

  val download: Flow[Url, CustomResponse, NotUsed] =
    Flow[Url].mapAsync[CustomResponse](Settings.Crawler.parallelism)(getRequestWithCustomResponse)

  val validateAndDownload: Flow[ReceivedMessage, CustomResponse, NotUsed] =
    toUrl via validate via download

  val printSink: Sink[CustomResponse, NotUsed] =
    Flow[CustomResponse].map(_.url).to(Sink.foreach(println))

  val extractUrls: Flow[CustomResponse, Url, NotUsed] =
    Flow[CustomResponse].mapConcat(from => getUrls(from))

  def getUrls(from: CustomResponse): List[Url] = (from.url, from.content, from.mediaType.map(_.fileExtensions contains "htm")) match {
    case (url, Some(content), Some(true)) => Jsoup.parse(content.map(_.toChar).mkString).select("a[href]").asScala.toList
      .filter(_.attr("href").length != 0)
      .map(
      element => {
        val href = element.attr("href").trim
        val depth = url.depth + 1
        if (href.startsWith("http")) Url(href,           depth, url.maxDepth, url._id, url.crawlRequestId)
        else                         Url(url.uri + href, depth, url.maxDepth, url._id, url.crawlRequestId)
      }
    )
    case _ => Nil
  }
}

