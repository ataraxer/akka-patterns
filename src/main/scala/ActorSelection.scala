package com.ataraxer.patterns.akka

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}

import scala.concurrent.Future

import AkkaApp._


object ActorSelection extends AkkaApp("actor-selection-app") {

  case class UselessMessage

  var reported = 0

  def isDone {
    if (reported == 6) {
      worker ! Shutdown
    }
  }

  class UselessActor extends Actor {
    def receive = {
      case _ => {
        println("Go away, I'm busy!")
        reported += 1
        isDone
      }
    }
  }

  class UselessParentActor extends UselessActor {
    private val subActorA = context.actorOf(Props[UselessActor], "sub-actor-a")
    private val subActorB = context.actorOf(Props[UselessActor], "sub-actor-b")
  }

  val actorA = system.actorOf(Props[UselessParentActor], "actor-a")
  val actorB = system.actorOf(Props[UselessParentActor], "actor-b")

  def run {
    // select all actors in the system
    val allActors = system.actorSelection("/user/*")
    // select all subactors in the system
    val subAllActors = system.actorSelection("/user/*/*")
    // make em all report
    allActors ! UselessMessage
    subAllActors ! UselessMessage
  }

}


// vim: set ts=2 sw=2 et:
