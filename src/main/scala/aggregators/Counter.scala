package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorSystem, ActorRefFactory, ActorRef, Props}


object Counter {
  type CountFilter = PartialFunction[Any, Boolean]

  case class Done
  case class Count(result: Int)
  case class DistinctCount(result: Map[Any, Int])

  case class Flush
}


trait CounterSpawner {
  import Counter._

  val defaultPF: CountFilter = {
    case _ => false
  }

  def count(objects: Any*)(implicit context: ActorRefFactory) =
    context.actorOf(Props(
      new Counter {
        def expected(msg: Any) = objects contains msg
      }
    ))


  def countPF(expect: CountFilter)
             (implicit context: ActorRefFactory) =
    context.actorOf(Props(
      new Counter {
        def expected(msg: Any) = (expect orElse defaultPF)(msg)
      }
    ))


  def expectCount(expectedSize: Int)(objects: Any*)
                 (implicit client: ActorRef, context: ActorRefFactory) =
    context.actorOf(Props(
      new Counter(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = objects contains msg
      }
    ))


  def expectCountPF(expectedSize: Int)(expect: CountFilter)
                   (implicit client: ActorRef, context: ActorRefFactory) =
    context.actorOf(Props(
      new Counter(client = Some(client), expectedSize = Some(expectedSize)) {
        def expected(msg: Any) = (expect orElse defaultPF)(msg)
      }
    ))


  def countDistinct(objects: Any*)(implicit context: ActorRefFactory) =
    context.actorOf(Props(
      new Counter(distinct = true) {
        def expected(msg: Any) = objects contains msg
      }
    ))


  def countDistinctPF(expect: CountFilter)
                     (implicit context: ActorRefFactory) =
    context.actorOf(Props(
      new Counter(distinct = true) {
        def expected(msg: Any) = (expect orElse defaultPF)(msg)
      }
    ))
}


abstract class Counter(distinct: Boolean = false,
                       expectedSize: Option[Int] = None,
                       client: Option[ActorRef] = None)
    extends Actor
{
  import Counter._

  private var result = Map.empty[Any, Int]

  def expected(msg: Any): Boolean

  def isDone = expectedSize match {
    case Some(size) => result.map(_._2).sum == size
    case None => false
  }

  def receive = {
    case Flush => {
      val response =
        if (distinct) DistinctCount(result.toMap)
                 else Count(result.map(_._2).sum)
      sender ! response
    }

    case msg if expected(msg) => {
      result += msg -> (result.getOrElse(msg, 0) + 1)
      if (isDone) client map { _ ! Done }
    }
  }
}


// vim: set ts=2 sw=2 et:
