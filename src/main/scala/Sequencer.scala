package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.kernel.Bootable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


case class Perform(list: List[Int])
case class Process(x: Int)
case class Result(x: Int)


object Sequencer {
  def props(client: ActorRef) = Props(new Sequencer(client))
}

class Sequencer(client: ActorRef) extends Actor {
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
      client ! Done
      println("Done!")
    }
  }
}


class FakeProcessor extends Actor {
  def receive = {
    case Process(x) => {
      println("Done", x)
      sender ! Done
    }
  }
}


class SequencerApp extends Bootable {
  val system = ActorSystem("somesystem")

  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(1.seconds)

  def startup {
    val worker = system.actorOf(Props(new Actor {
      val sequencer = context.actorOf(Sequencer.props(self), "sequencer")
      def receive = {
        case Work => sequencer ! Perform(List(1, 2, 3, 4))
        case Done => shutdown()
      }
    }))

    println("Initialized")

    worker ! Work
  }

  def shutdown {
    system.shutdown()
  }
}


object SequencerApp extends Application {
  (new SequencerApp).startup()
}


// vim: set ts=2 sw=2 et:
