import sbt._
import sbt.Keys._


object AkkaPattersBuild extends Build {

  val akkaVersion = "2.3.1"
  val scalatestVersion = "2.1.0"

  lazy val commonSettings = Seq(
    scalacOptions ++= Seq(
      "-g:vars",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-Xfatal-warnings"
    ),

    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(
      // Akka
      "com.typesafe.akka" %% "akka-actor"   % akkaVersion,
      "com.typesafe.akka" %% "akka-kernel"  % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"   % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote"  % akkaVersion,
      // ScalaTest
      "org.scalatest"     %% "scalatest"    % scalatestVersion  % "test",
      // ZooKeeper
      "org.apache.zookeeper" % "zookeeper" % "3.3.6" excludeAll (
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx"),
        ExclusionRule(organization = "javax.jms")
      ),
      // Kafka
      "org.apache.kafka" %% "kafka" % "0.8.1",
      "log4j" % "log4j" % "1.2.15" excludeAll (
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx"),
        ExclusionRule(organization = "javax.jms")
      ),
      "org.slf4j" % "slf4j-log4j12" % "1.7.5" exclude("org.slf4j", "slf4j-simple")
    ),

    parallelExecution := false
  )

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    name         := "akka-patterns",
    version      := "0.1.0",
    scalaVersion := "2.10.3"
  )

  lazy val akkaPatterns = Project(
    id = "akka-patterns",
    base = file("."),
    settings = buildSettings
  ).settings(
    commonSettings: _*
  )

}


// vim: set ts=2 sw=2 et:
