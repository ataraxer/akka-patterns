package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorSystem, ActorRefFactory, ActorRef, Props}
import akka.actor.Actor.Receive
import akka.pattern.{ask, pipe}

import scala.reflect.ClassTag

import AkkaApp._


object Collector {
  type Extractor = PartialFunction[Any, Any]
  case class Collection(result: Seq[Any])
  case class Flush
}


trait CollectorSpawner {
  import Collector._

  def collect(objects: Any*)(implicit context: ActorRefFactory) =
    context.actorOf(Props(
      new Collector {
        def expected(msg: Any) = objects contains msg
      }
    ))


  def collectPF(extractor: Extractor)
               (implicit context: ActorRefFactory) =
    context.actorOf(Props(
      new Collector {
        def expected(msg: Any) = extractor.isDefinedAt(msg)
        override def extract(msg: Any) = extractor(msg)
      }
    ))


  def collectCount(expectedSize: Int)(objects: Any*)
                  (implicit client: ActorRef, context: ActorRefFactory) =
    context.actorOf(Props(
      new Collector(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = objects contains msg
      }
    ))


  def collectCountPF(expectedSize: Int)(extractor: Extractor)
                    (implicit client: ActorRef, context: ActorRefFactory) =
    context.actorOf(Props(
      new Collector(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = extractor.isDefinedAt(msg)
        override def extract(msg: Any) = extractor(msg)
      }
    ))
}


abstract class Collector(expectedSize: Option[Int] = None,
                         client: Option[ActorRef]  = None)
    extends Actor with Spawner
{
  import Collector._

  private var result = Seq.empty[Any]

  def expected(msg: Any): Boolean
  def extract(msg: Any): Any = msg

  def isDone = expectedSize match {
    case Some(size) => result.size == size
    case None => false
  }

  def receive = {
    case Flush => sender ! Collection(result)

    case msg if expected(msg) => {
      result :+= extract(msg)
      if (isDone) client map { _ ! Collection(result) }
    }
  }
}


// vim: set ts=2 sw=2 et:
