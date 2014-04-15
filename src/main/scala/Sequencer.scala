package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, ActorLogging, Props, Terminated}
import akka.pattern.{ask, pipe}

import AkkaApp._


object Sequencer {
  case class Perform[T](list: List[T])
  case object Done
}

class Sequencer(client: ActorRef, processor: ActorRef)
    extends Actor
    with Spawner
{
  import Sequencer._

  context.watch(client)

  def receive = {
    case Perform(x :: xs) => {
      val handler = handle {
        case Done => self ! Perform(xs)
      }

      processor.tell(x, handler)
    }

    case Perform(Nil) => {
      client ! Done
    }

    case Terminated(_) => context.stop(self)
  }
}


// vim: set ts=2 sw=2 et:
