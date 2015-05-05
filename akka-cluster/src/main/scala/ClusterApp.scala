package playground.akka.cluster

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster


object ClusterApp extends App {
  System.setProperty("akka.remote.netty.tcp.port", "9090")
  val system = ActorSystem("cluster-app")
  Cluster(system) registerOnMemberUp println("cluster is running")
}


// vim: set ts=2 sw=2 et:
