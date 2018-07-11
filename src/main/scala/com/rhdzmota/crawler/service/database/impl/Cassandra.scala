package com.rhdzmota.crawler.service.database.impl


import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Sink}
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
import com.rhdzmota.crawler.model.{CustomResponse, Url}
import com.rhdzmota.crawler._
import com.rhdzmota.crawler.service.database.Database

import scala.concurrent.Future

case object Cassandra extends Database[CustomResponse, Future[Done]] with Context {

  implicit private val session: Session = Cluster.builder
    .addContactPoint(Settings.Cassandra.address)
    .withPort(Settings.Cassandra.port)
    .build.connect()

  private val insertQuery: String =
    s"""
       |INSERT INTO ${Settings.Cassandra.keyspaceName}.${Settings.Cassandra.urlTable}(id, uri, depth, max_depth, from_url, crawl_request_id, timestamp)
       |VALUES (?, ?, ?, ?, ?, ?, ?)
     """.stripMargin

  private val preparedStatement: PreparedStatement =
    session.prepare(insertQuery)

  private val statementBinder: (Url, PreparedStatement) => BoundStatement =
    (url: Url, statement: PreparedStatement) => statement.bind(
      url._id, url.uri, url.depth.asInstanceOf[java.lang.Integer], url.maxDepth.asInstanceOf[java.lang.Integer], url.from, url.crawlRequestId, url.timestamp)

  val connectorToSink: Flow[CustomResponse, Url, NotUsed] =
    Flow[CustomResponse].map(customResponse => customResponse.url)

  val sink: Sink[Url, Future[Done]] = CassandraSink[Url](
    Settings.Cassandra.parallelism,
    preparedStatement,
    statementBinder)

}
