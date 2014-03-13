name := "akka-patterns"

version := "0.1.0"

scalaVersion := "2.10.3"

mainClass := Some("Game")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.0"

libraryDependencies += "com.typesafe.akka" %% "akka-kernel" % "2.3.0"
