package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorSystem, ActorRef, Props}
import akka.actor.Actor.Receive
import akka.pattern.{ask, pipe}

import scala.reflect.ClassTag

import AkkaApp._


object Counter {
  case class Done
  case class Flush
  case class Count(result: Map[Any, Int])
}


trait CounterSpawner {
  def countPF(expect: Receive)(implicit context: ActorSystem) = {
    context.actorOf(Props(
      new Counter { def expected(msg: Any) = expect.isDefinedAt(msg) }
    ))
  }

  def count(classes: Class[_]*)(implicit context: ActorSystem) = {
    context.actorOf(Props(
      new Counter {
        def expected(msg: Any) = classes contains msg.getClass
      }
    ))
  }

  def count[A <: Any : ClassTag] = {
    val clazz = implicitly[ClassTag[A]].runtimeClass
    new CounterBuilder {
      def expected(msg: Any) = {
        // TODO: support primitives like int and float
        clazz.isInstance(msg) || clazz == msg.getClass
      }
    }
  }

  abstract class CounterBuilder {
    def expected(msg: Any): Boolean

    def +(that: CounterBuilder) = {
      val me = this
      new CounterBuilder {
        def expected(msg: Any) = {
          me.expected(msg) || that.expected(msg)
        }
      }
    }
  }

  implicit def builderToActor(builder: CounterBuilder)
                             (implicit system: ActorSystem) =
    system.actorOf(Props(
      new Counter {
        def expected(msg: Any) = builder.expected(msg)
      }
    ))
}


abstract class Counter extends Actor with Spawner {
  import Counter._

  private var result = Map.empty[Any, Int]

  def expected(msg: Any): Boolean

  def receive = {
    case Flush => sender ! Count(result.toMap)

    case msg if expected(msg) => {
      result += msg -> (result.getOrElse(msg, 0) + 1)
    }
  }
}


// vim: set ts=2 sw=2 et:
