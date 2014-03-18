package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}

import AkkaApp._


object Sequencer {
  case class Perform(list: List[Int])
  case class Process(x: Int)
  case class Result(x: Int)
  case class Done
}

class Sequencer extends Actor with Spawner {
  import Sequencer._
  import context._

  val processor = context.actorOf(Props[FakeProcessor], "processor")

  private var client: ActorRef = null

  def receive = {
    case msg: Perform => {
      client = sender
      become(active)
      self ! msg
    }
  }

  def active: Receive = {
    case Perform(x :: xs) => {
      val handler = handle {
        case Done => self ! Perform(xs)
      }

      processor.tell(Process(x), handler)
    }

    case Perform(Nil) => {
      client ! Shutdown
      println("Done!")
    }
  }
}


class FakeProcessor extends Actor {
  import Sequencer._

  def receive = {
    case Process(x) => {
      println("Processed " + x)
      sender ! Done
    }
  }
}


object SequencerApp extends AkkaApp("sequencer-app") {
  def run {
    val sequencer = system.actorOf(Props[Sequencer], "sequencer")
    sequencer ! Sequencer.Perform(List(1, 2, 3, 4))
  }
}


// vim: set ts=2 sw=2 et:
