package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.kernel.Bootable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


case class ReportStatus
case class Print(msg: String)
case class Work
case class Done


class Reporter extends Actor {
  def receive = {
    case ReportStatus => sender ! "I'm alive!"
  }
}


class Printer extends Actor {
  def receive = {
    case Print(msg) => {
      println(msg)
      sender ! Done
    }
  }
}


class AkkaApp extends Bootable {
  val system = ActorSystem("somesystem")
  val reporter = system.actorOf(Props[Reporter], "reporter")

  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(1.seconds)

  def startup {
    val futureStatus = reporter ? ReportStatus
    val msg = for (status <- futureStatus.mapTo[String]) yield Print(status)

    val worker = system.actorOf(Props(new Actor {
      def receive = {
        case Work => msg pipeTo context.actorOf(Props[Printer])
        case Done => shutdown()
      }
    }))

    worker ! Work
  }

  def shutdown {
    system.shutdown()
  }
}


object Bootstrap extends Application {
  (new AkkaApp).startup()
}


// vim: set ts=2 sw=2 et:
