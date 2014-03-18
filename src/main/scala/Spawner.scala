package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorContext, Props}
import akka.actor.Actor.Receive


/*
 * Spawner trait defines two concrete methods `spawn'
 * and `handle' which produce new actor in a context
 * of current actor and new anonymous actor that is
 * used for handling some response.
 */
trait Spawner {
  val context: ActorContext

  def spawn(creator: => Actor) =
    context.actorOf(Props(creator))

  def handle(receiver: Receive) =
    spawn {
      new Actor {
        def receive = receiver
      }
    }
}


// vim: set ts=2 sw=2 et:
