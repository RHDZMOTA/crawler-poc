package com.rhdzmota.crawler

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnNext}
import akka.stream.actor._
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import com.rhdzmota.crawler.model.Url
import com.rhdzmota.crawler.util.ClientHttp
import org.jsoup.Jsoup

import scala.concurrent.Future
import scala.collection.JavaConverters._


object Crawler extends ClientHttp with Context {

  val parallelism = 5

  val source: Source[Url, NotUsed] = Source(List(Url("https://github.com", 0))) // Use actors

  val sink: Sink[Url, Future[Done]] = Sink.foreach(println) // Use actors

  val graph: RunnableGraph[NotUsed] = source
    .mapAsync[(Url, Option[String])](parallelism)(getRequestAsString)
    .mapConcat(from => getUrls(from)).to(sink)

  def main(args: Array[String]): Unit = {

    //graph.run()


    //val r = Source.actorRef(Int.MaxValue, OverflowStrategy.backpressure)

    val actorRef = actorSystem.actorOf(Props[PublishActor])
    val publisher = ActorPublisher(actorRef)
    val subscriberActor = actorSystem.actorOf(Props[SubscribeActor])

    val g: RunnableGraph[NotUsed] = RunnableGraph.fromGraph {
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._
        val source: Source[Url, NotUsed] = Source.fromPublisher(publisher)
          .mapAsync[(Url, Option[String])](parallelism)(getRequestAsString)
          .mapConcat(from => getUrls(from))
        val sink: Sink[Url, NotUsed] = Sink.fromSubscriber(ActorSubscriber[Url](subscriberActor))
        source ~> sink
        ClosedShape
      }
    }

    g.run()

    Thread.sleep(100)

    actorRef ! Url("https://github.com", 0)

  }

  def actors = {

    object MirrorActor {
      case object Ack
      case object StreamInitialized
      case object StreamCompleted
      final case class StreamFailure(ex: Throwable)
    }

    val bufferSize = 10 // Number of messages
    val maxDepth = 2

    val source = Source.actorRef[Url](bufferSize, OverflowStrategy.backpressure)

    val mirrorActor = actorSystem.actorOf(Props[MirrorActor])

    val sink = Sink.actorRefWithAck(mirrorActor, MirrorActor.StreamInitialized, MirrorActor.Ack, MirrorActor.StreamCompleted)


    val validUrls = source.filter(url => url.depth <= maxDepth)

    val t = validUrls.to(Sink.ignore).run()


    sink

    class MirrorActor(stream: ActorRef) extends Actor {
      override def receive: Receive = {
        case url @ Url(_, _) =>
          println("Url received: " + url.toString)
          stream ! url
      }
    }

    t
    source
  }

  class PublishActor extends Actor with ActorPublisher[Url] {
    override def receive: Receive = {
      case url: Url => url.depth match {
        case finnish if finnish > 2 => onComplete()
        case _ => onNext(url)
      }
    }
  }

  class SubscribeActor extends ActorSubscriber {
    override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy
    override def receive: Receive = {
      case OnNext(url) =>
        println(url)
      case OnComplete =>
        println("Complete!")
    }
  }

  def getUrls(from: (Url, Option[String])): List[Url] = from match {
    case (_, None) => Nil
    case (url, Some(response)) => Jsoup.parse(response).select("a[href]").asScala.toList
      .filter(_.attr("href").length != 0)
      .map(
      element => {
        val href = element.attr("href").trim
        val depth = url.depth + 1
        if (href.startsWith("https")) Url(href, depth)
        else Url(url.uri + href, depth)
      }
    )
  }
}

