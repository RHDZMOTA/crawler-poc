package com.rhdzmota.crawler.model

import akka.http.scaladsl.model.MediaType

case class CustomResponse(url: Url, content: Option[Array[Byte]], mediaType: Option[MediaType])
