package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}

import AkkaApp._


object Aggregator {
  case class Process(x: Int)
  case class Result(x: Int)
  case class PerformList(xs: List[Int])
  case class ResultList(xs: List[Int])

  class Doubler extends Actor {
    def receive = {
      case Process(x) => sender ! Result(x * 2)
    }
  }
}

class Aggregator extends Actor with Spawner {
  import Aggregator._
  import context._

  val processor = context.actorOf(Props[Doubler], "processor")

  private var result = List.empty[Int]

  def receive = {
    case PerformList(xs: List[Int]) => {
      val client = sender

      val handler = handle {
        case Result(x) => {
          result +:= x
          if (result.size == xs.size) {
            println(result)
            client ! ResultList(result)
          }
        }
      }

      for (x <- xs) {
        processor.tell(Process(x), handler)
      }
    }
  }
}


// vim: set ts=2 sw=2 et:
