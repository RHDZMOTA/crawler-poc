package com.rhdzmota.crawler.service.pubsub.impl

import java.util.UUID
import akka.stream.alpakka.googlecloud.pubsub.{AcknowledgeRequest, PubSubMessage, PublishRequest, ReceivedMessage}
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.rhdzmota.crawler.model.Url
import com.rhdzmota.crawler._

import scala.concurrent.Future
import scala.collection.immutable.Seq

import com.rhdzmota.crawler.service.pubsub.PubSub

case object GCPubSub extends PubSub[ReceivedMessage, AcknowledgeRequest, Future[Done], Future[Seq[Seq[String]]]] with Context {
    val source: Source[ReceivedMessage, NotUsed] =
      GooglePubSub.subscribe(
        Settings.PubSub.projectId,
        Settings.PubSub.apiKey,
        Settings.PubSub.clientEmail,
        Settings.PubSub.privateKey,
        Settings.Crawler.pubSubSubscription)

    val ackRequest: Flow[ReceivedMessage, AcknowledgeRequest, NotUsed] =
      Flow[ReceivedMessage].map(message => message.ackId)
        .groupedWithin(Settings.PubSub.acknowledgeGroupingSize, Settings.PubSub.acknowledgeGroupingTime)
        .map(AcknowledgeRequest.apply)

    val ackSink: Sink[AcknowledgeRequest, Future[Done]] =
      GooglePubSub.acknowledge(
        Settings.PubSub.projectId,
        Settings.PubSub.apiKey,
        Settings.PubSub.clientEmail,
        Settings.PubSub.privateKey,
        Settings.Crawler.pubSubSubscription)

    val publishFlow: Flow[PublishRequest, Seq[String], NotUsed] =
      GooglePubSub.publish(
        Settings.PubSub.projectId,
        Settings.PubSub.apiKey,
        Settings.PubSub.clientEmail,
        Settings.PubSub.privateKey,
        Settings.Crawler.pubSubTopic)

    val sink: Sink[Url, Future[Seq[Seq[String]]]] =
      Flow[Url].map(url => PubSubMessage("crwl", UUID.randomUUID().toString, Some(url.toMap)))
        .groupedWithin(Settings.PubSub.publishGroupingSize, Settings.PubSub.publishGroupingTime)
        .map(PublishRequest(_))
        .via(publishFlow).toMat(Sink.seq)(Keep.right)

    def publish(url: Url): Future[Seq[Seq[String]]] = {
      val publishMessage = PubSubMessage("crwl", UUID.randomUUID().toString, Some(url.toMap))
      val publishRequest = PublishRequest(Seq(publishMessage))
      Source.single(publishRequest).via(publishFlow).runWith(Sink.seq)
    }
}
