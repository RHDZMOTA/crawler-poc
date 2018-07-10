package com.rhdzmota.crawler.util

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.rhdzmota.crawler.model.{CustomResponse, Url}

import scala.concurrent.{ExecutionContext, Future}

trait ClientHttp {

  def getRequest(url: Url)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Future[(Url, HttpResponse)] =
    Http(actorSystem).singleRequest(
      HttpRequest(HttpMethods.GET, url.uri)
    ).map(url -> _)

  def getRequestWithCustomResponse(url: Url)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext, materializer: Materializer): Future[CustomResponse] = for {
    httpResponse  <- Http(actorSystem).singleRequest(HttpRequest(HttpMethods.GET, url.uri))
    content       <- Unmarshal(httpResponse.entity).to[Array[Byte]]
  } yield
    if (httpResponse.status.isSuccess())
      CustomResponse(url, Some(content), Some(httpResponse.entity.contentType.mediaType))
    else
      CustomResponse(url, None, None)
}
