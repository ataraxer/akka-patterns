package com.ataraxer.patterns.akka

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}

import AkkaApp._


case class ReportStatus
case class Print(msg: String)


class Reporter extends Actor {
  def receive = {
    case ReportStatus => sender ! "I'm alive!"
  }
}


class Printer extends Actor {
  def receive = {
    case Print(msg) => {
      println(msg)
      sender ! Shutdown
    }
  }
}


object Bootstrap extends AkkaApp("bootstrap-app") {
  val reporter = system.actorOf(Props[Reporter], "reporter")

  def run {
    val futureStatus = reporter ? ReportStatus
    val msg = for (status <- futureStatus.mapTo[String]) yield Print(status)

    msg pipeTo system.actorOf(Props[Printer])
  }
}


// vim: set ts=2 sw=2 et:
