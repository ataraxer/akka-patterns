val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-g:vars",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-Xfatal-warnings"),

  scalaVersion := "2.11.6")


val akkaVersion = "2.3.9"


val akka = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")


lazy val akkaCluster = (project in file("akka-cluster"))
  .settings(commonSettings: _*)
  .settings(Revolver.settings: _*)
  .settings(name := "akka-cluster-playground")
  .settings(parallelExecution := false)
  .settings(libraryDependencies ++= akka ++ Seq(
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion))

