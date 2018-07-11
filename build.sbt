import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.rhdzmota",
      scalaVersion := "2.12.6",
      version      := "1.1.0"
    )),
    name := "crawler-poc",
    libraryDependencies ++= {
      val akkaVersion = "2.5.13"
      val akkaHttpVersion = "10.1.1"
      val jsoupVersion = "1.11.3"
      val configVersion = "1.3.1"
      val googleCloudVersion = "1.31.0"
      val cassandraAlpakkaVersion = "0.19"
      val gcloudAlpakkaPubSubVersion = "0.19"
      Seq(
        "org.jsoup" % "jsoup" % jsoupVersion,
        "com.typesafe" % "config" % configVersion,
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
        "com.google.cloud" % "google-cloud-storage" % googleCloudVersion,
        "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % cassandraAlpakkaVersion,
        "com.lightbend.akka" %% "akka-stream-alpakka-google-cloud-pub-sub" % gcloudAlpakkaPubSubVersion,
        scalaTest % Test
      )
    }
  )
