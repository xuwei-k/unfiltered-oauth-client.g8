organization := "com.example"

name := "$name$"

scalaVersion := "$scala_version$"

scalacOptions ++= "-deprecation" :: Nil

version := "0.1.0-SNAPSHOT"

val unfilteredVersion = "$unfiltered_version$"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % unfilteredVersion,
  "net.databinder" %% "unfiltered-jetty" % unfilteredVersion,
  "net.databinder" %% "unfiltered-json4s" % unfilteredVersion,
  "net.databinder" %% "dispatch-oauth" % "$dispatch_version$",
  "ch.qos.logback" % "logback-classic" % "$logback_version$",
  "net.liftweb" %% "lift-json" % "$lift_json_version$"
)
