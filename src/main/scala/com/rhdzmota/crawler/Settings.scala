package com.rhdzmota.crawler

import com.typesafe.config.{Config, ConfigFactory}
import java.security.{KeyFactory, PrivateKey}
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import scala.concurrent.duration._

object Settings {
  private val app: Config = ConfigFactory.load().getConfig("application")

  object PubSub {
    private val pubsub: Config            = app.getConfig("pubsub")
    val privateKeyString: String          = pubsub.getString("privateKey")
    val projectId: String                 = pubsub.getString("projectId")
    val apiKey: String                    = pubsub.getString("apiKey")
    val clientEmail: String               = pubsub.getString("clientEmail")
    val acknowledgeGroupingSize: Int      = pubsub.getInt("acknowledgeGroupingSize ")
    val acknowledgeGroupingTime: FiniteDuration = pubsub.getInt("acknowledgeGroupingTime").seconds
    val publishGroupingSize : Int               = pubsub.getInt("publishGroupingSize")
    val publishGroupingTime: FiniteDuration     = pubsub.getInt("publishGroupingTime").seconds
    val privateKey: PrivateKey                  = KeyFactory.getInstance("RSA").generatePrivate(
      new PKCS8EncodedKeySpec(Base64.getDecoder.decode(privateKeyString)))
 }
  object Cassandra {
    private val cassandra: Config = app.getConfig("cassandra")
    val address: String           = cassandra.getString("address")
    val port: Int                 = cassandra.getInt("port")
    val keyspaceName: String      = cassandra.getString("keyspaceName")
    val urlTable: String          = cassandra.getString("urlTable")
    val parallelism: Int          = cassandra.getInt("parallelism")
  }
  object Storage {
    private val storage: Config   = app.getConfig("storage")
    val bucketName: String        = storage.getString("bucketName")
    val projectId: String         = storage.getString("projectId")
    val privateKeyId: String      = storage.getString("privateKeyId")
    val privateKeyString: String  = storage.getString("privateKey")
    val clientEmail: String       = storage.getString("clientEmail")
    val clientId: String          = storage.getString("clientId")
    val privateKey: PrivateKey    = KeyFactory.getInstance("RSA").generatePrivate(
      new PKCS8EncodedKeySpec(Base64.getDecoder.decode(privateKeyString)))
  }
  object Crawler {
    private val crawler: Config     = app.getConfig("crawler")
    val parallelism: Int            = crawler.getInt("parallelism")
    val depth: Int                  = crawler.getInt("depth")
    val pubSubSubscription: String  = crawler.getString("pubSubSubscription")
    val pubSubTopic: String         = crawler.getString("pubSubTopic")
    val urlLifeSpan: FiniteDuration = crawler.getInt("urlLifeSpan").seconds
    val askTimeout: FiniteDuration  = crawler.getInt("askTimeout").seconds
  }
}
