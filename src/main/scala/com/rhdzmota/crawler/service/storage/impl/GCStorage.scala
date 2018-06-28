package com.rhdzmota.crawler.service.storage.impl

import com.rhdzmota.crawler.service.storage.StorageTrait
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.{Blob, BlobInfo, Storage, StorageOptions}
import com.rhdzmota.crawler.model.Url
import com.rhdzmota.crawler._

case object GCStorage extends StorageTrait[(Url, Option[String]), Array[Byte], Blob] with Context {
    private val credentials: Credentials = new ServiceAccountCredentials(
      Settings.Storage.clientId,
      Settings.Storage.clientEmail,
      Settings.Storage.privateKey,
      Settings.Storage.privateKeyId,
      null
    )
    private val storage: Storage = StorageOptions.newBuilder().setCredentials(credentials).build.getService

    def uploadFile(fileName: String, content: Array[Byte]): Blob = storage.create(
      BlobInfo.newBuilder(Settings.Storage.bucketName, fileName).build(),
      content
    )

    val toFile: Flow[(Url, Option[String]), (String, Array[Byte]), NotUsed] =
      Flow[(Url, Option[String])].filter({case (_, optStr) => optStr.isDefined}).map({
        case (url, Some(str)) => (url._id.toString, str.getBytes)
        case (url, _)         => (url._id.toString, "None".getBytes)
      })

    val sink: Sink[(String, Array[Byte]), NotUsed] =
      Flow[(String, Array[Byte])].to(Sink.foreach({
        case (fileName, content) => uploadFile(fileName, content)
    }))

}
