package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, ActorLogging, Props, Terminated, PoisonPill}

import AkkaApp._


object WatcherApp extends AkkaApp("watcher-app") {

  case class Watch(actor: ActorRef)

  class UselessActor extends Actor with ActorLogging {
    def receive = {
      case Watch(actor) => context.watch(actor)
      case Terminated(deadOne) => {
        log.info("OMG, {} is dead!", deadOne)
        worker.tell(Shutdown, self)
      }
    }
  }

  val actorA = system.actorOf(Props[UselessActor], "actor-a")
  val actorB = system.actorOf(Props[UselessActor], "actor-b")

  def run {
    actorA ! Watch(actorB)
    actorB ! PoisonPill
  }
}


// vim: set ts=2 sw=2 et:
