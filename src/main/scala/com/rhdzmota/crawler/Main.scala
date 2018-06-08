package com.rhdzmota.crawler

object Main {

  def main(args: Array[String]): Unit = {
    val seedUri = args.headOption.getOrElse("https://github.com")
  }
}
