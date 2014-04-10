package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorSystem, ActorRefFactory, ActorRef, Props}


object Collector {
  type Extractor = PartialFunction[Any, Any]
  case class Collection(result: Seq[Any])
  case object Flush
  case object Peak
}


trait CollectorSpawner {
  import Collector._

  val context: ActorRefFactory

  def collect(objects: Any*) =
    context.actorOf(Props(
      new Collector {
        def expected(msg: Any) = objects contains msg
      }
    ))


  def collectPF(extractor: Extractor) =
    context.actorOf(Props(
      new Collector {
        def expected(msg: Any) = extractor.isDefinedAt(msg)
        override def extract(msg: Any) = extractor(msg)
      }
    ))


  def collectCount(expectedSize: Int)(objects: Any*)
                  (implicit client: ActorRef) =
    context.actorOf(Props(
      new Collector(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = objects contains msg
      }
    ))


  def collectCountPF(expectedSize: Int)(extractor: Extractor)
                    (implicit client: ActorRef) =
    context.actorOf(Props(
      new Collector(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = extractor.isDefinedAt(msg)
        override def extract(msg: Any) = extractor(msg)
      }
    ))
}


abstract class Collector(expectedSize: Option[Int] = None,
                         client: Option[ActorRef]  = None)
    extends Actor
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
    case Flush => {
      sender ! Collection(result)
      result = Seq.empty[Any]
    }

    case Peak => sender ! Collection(result)

    case msg if expected(msg) => {
      result :+= extract(msg)
      if (isDone) {
        client map { _ ! Collection(result) }
        context.stop(self)
      }
    }
  }
}


// vim: set ts=2 sw=2 et:
