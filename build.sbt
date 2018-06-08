import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.rhdzmota",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "crawler-poc",
    libraryDependencies ++= {
      val akkaVersion = "2.5.13"
      val akkaHttpVersion = "10.1.1"
      val jsoupVersion = "1.11.3"
      val configVersion = "1.3.1"

      Seq(
        "org.jsoup" % "jsoup" % jsoupVersion,
        "com.typesafe" % "config" % configVersion,
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
        scalaTest % Test
      )
    }
  )
