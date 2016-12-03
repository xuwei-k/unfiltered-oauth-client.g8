package com.example

/** oauth client */
object Client {
  val port = 8081
  val consumer = dispatch.classic.oauth.Consumer("key", "secret")
  def resources = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")

  def main(args: Array[String]) {
    val binding = unfiltered.jetty.SocketPortBinding(port, "localhost")
    unfiltered.jetty.Server(binding :: Nil, Nil, None)
      .resources(Client.resources)
      .plan(new App(consumer)).run
  }
}
