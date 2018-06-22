package com.rhdzmota.crawler.util

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.rhdzmota.crawler.model.Url

import scala.concurrent.{ExecutionContext, Future}

trait ClientHttp {

  def getRequest(url: Url)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Future[(Url, HttpResponse)] =
    Http(actorSystem).singleRequest(
      HttpRequest(HttpMethods.GET, url.uri)
    ).map(url -> _)

  def getRequestAsString(url: Url)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext, materializer: Materializer): Future[(Url, Option[String])] = for {
    httpResponse <- Http(actorSystem).singleRequest(HttpRequest(HttpMethods.GET, url.uri))
    contentString <- Unmarshal(httpResponse.entity).to[String]
  } yield if (httpResponse.status.isSuccess()) url -> Some(contentString) else url -> None
}
