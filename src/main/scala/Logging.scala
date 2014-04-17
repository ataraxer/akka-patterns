package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, ActorLogging, Props, Terminated, PoisonPill}
import kafka.utils.Logging

import AkkaApp._


object LoggingApp extends AkkaApp("logging-app") {

  case class Log(msg: String)
  case object Done

  def time = System.currentTimeMillis()

  class GoodLogger extends Actor with ActorLogging {
    var start: Long = time

    def receive = {
      case Log(msg) => log.info(msg)
      case Done     =>
        println("Good done in %s".format(time - start))
    }
  }

  class BadLogger extends Actor with Logging {
    var start: Long = time

    def receive = {
      case Log(msg) => info(msg)
      case Done     =>
        println("Bad done in %s".format(time - start))
    }
  }

  def run {
    val good = system.actorOf(Props[GoodLogger], "actor-a")
    val bad  = system.actorOf(Props[BadLogger], "actor-b")
    val messages = Vector.fill(1000000) { Log("bullshit") }
    messages foreach { good ! _ }
    messages foreach { bad  ! _ }
    good ! Done
    bad  ! Done
  }
}


// vim: set ts=2 sw=2 et:
