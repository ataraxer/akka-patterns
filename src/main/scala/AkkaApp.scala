package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration._


object AkkaApp {
  case class Startup
  case class Shutdown
}

abstract class AkkaApp(appName: String) {
  import AkkaApp._

  protected val system = ActorSystem(appName)

  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(1.seconds)

  def run: Unit
  def stop = worker ! Shutdown

  protected implicit val worker = system.actorOf(
    Props(new Actor {
      def receive = {
        case Startup  => {
          println("Initialized " + appName)
          run
        }
        case Shutdown => {
          println("Terminated " + appName)
          system.shutdown()
        }
      }
    })
  )

  def main(args: Array[String]) {
    worker ! Startup
  }
}


// vim: set ts=2 sw=2 et:
