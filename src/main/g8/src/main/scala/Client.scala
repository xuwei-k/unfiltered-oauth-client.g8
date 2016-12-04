package com.example

import com.typesafe.scalalogging.StrictLogging

/** oauth client */
object Client extends StrictLogging {
  val port = 8081
  val consumer = dispatch.classic.oauth.Consumer("key", "secret")
  def resources = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")

  def main(args: Array[String]) {
    logger.info("starting unfiltered oauth consumer at localhost on port " + port)
    val binding = unfiltered.jetty.SocketPortBinding(port, "localhost")
    unfiltered.jetty.Server.portBinding(binding)
      .resources(Client.resources)
      .plan(new App(consumer)).run
  }
}
