package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}

import AkkaApp._


object Sequencer {
  def props(client: ActorRef) = Props(new Sequencer(client))
  case class Perform(list: List[Int])
  case class Process(x: Int)
  case class Result(x: Int)
  case class Done
}

class Sequencer(client: ActorRef) extends Actor {
  import Sequencer._

  val processor = context.actorOf(Props[FakeProcessor], "processor")

  def handle(receiver: PartialFunction[Any, Unit]) =
    context.actorOf(Props(new Actor {
      def receive = receiver
    }))

  def receive = {
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
      println("Done", x)
      sender ! Done
    }
  }
}


object SequencerApp extends AkkaApp("sequencer-app") {
  def run {
    val sequencer = system.actorOf(Sequencer.props(worker), "sequencer")
    sequencer ! Sequencer.Perform(List(1, 2, 3, 4))
  }
}


// vim: set ts=2 sw=2 et:
