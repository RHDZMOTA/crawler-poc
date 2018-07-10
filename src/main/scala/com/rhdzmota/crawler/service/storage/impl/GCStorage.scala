package com.rhdzmota.crawler.service.storage.impl

import com.rhdzmota.crawler.service.storage.StorageTrait
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.{Blob, BlobInfo, Storage, StorageOptions}
import com.rhdzmota.crawler.model.{CustomResponse, Url}
import com.rhdzmota.crawler._

case object GCStorage extends StorageTrait[CustomResponse, Array[Byte], Blob] with Context {
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

    val toFile: Flow[CustomResponse, (String, Array[Byte]), NotUsed] =
      Flow[CustomResponse].filter(_.content.isDefined).map( customResponse => {
        val fileName: String = customResponse.url._id.toString + customResponse.mediaType.flatMap(_.fileExtensions.lift(0).map("." + _)).getOrElse("")
        val fileContent: Array[Byte] = customResponse.content.getOrElse("None".getBytes)
        (fileName, fileContent)
      })

    val sink: Sink[(String, Array[Byte]), NotUsed] =
      Flow[(String, Array[Byte])].to(Sink.foreach({
        case (fileName, content) => uploadFile(fileName, content)
    }))

}
