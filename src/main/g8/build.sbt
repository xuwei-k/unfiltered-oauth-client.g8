organization := "com.example"

name := "$name$"

scalaVersion := "$scala_version$"

val unusedWarnings = (
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)

scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
  case Some((2, v)) if v >= 11 => unusedWarnings
}.toList.flatten

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)

scalacOptions ++= "-deprecation" :: "unchecked" :: "-feature" :: Nil

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
