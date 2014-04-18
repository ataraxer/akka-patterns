package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, ActorLogging, Props, Terminated, PoisonPill}
import kafka.utils.Logging

import AkkaApp._


object LoggingApp extends AkkaApp("logging-app") {

  case class Log(msg: String)
  case object Done

  def time = System.currentTimeMillis()

  trait TimedActor extends Actor {
    val start: Long = time

    def receive = {
      case Done     =>
        println("%s done in %s".format(self.path.name, time - start))
    }
  }


  class GoodLogger extends Actor with ActorLogging with TimedActor {
    override def receive = super.receive orElse {
      case Log(msg) => log.info(msg)
    }
  }


  class BadLogger extends Actor with Logging with TimedActor {
    override def receive = super.receive orElse {
      case Log(msg) => info(msg)
    }
  }


  def run {
    val good = system.actorOf(Props[GoodLogger], "good-logger")
    val bad  = system.actorOf(Props[BadLogger],  "bad-logger")
    val messages = Vector.fill(1000000) { Log("bullshit") }
    messages foreach { good ! _ }
    messages foreach { bad  ! _ }
    good ! Done
    bad  ! Done
  }
}


// vim: set ts=2 sw=2 et:
