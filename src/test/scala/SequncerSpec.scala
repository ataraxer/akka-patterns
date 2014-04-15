package com.ataraxer.patterns.test.akka

import org.scalatest._

import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import akka.actor.{Actor, ActorRef, ActorSystem}

import com.ataraxer.patterns.akka._


object SequencerSpec {
  class Item
  case object First extends Item
  case object Second extends Item
  case object Last extends Item
  val testSequence = List(First, Second, Last)

  class Echoer(client: ActorRef) extends Actor {
    def receive = {
      case msg => {
        client ! msg
        sender ! Sequencer.Done
      }
    }
  }
}


class SequencerSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
{
  import SequencerSpec._

  val echoer = TestActorRef(new Echoer(testActor))
  val sequencer = TestActorRef(new Sequencer(testActor, echoer))

  def this() = this(ActorSystem("sequencer-spec"))

  "A Sequencer" should "perform actions sequntially" in {
    sequencer ! Sequencer.Perform(testSequence)
    expectMsg(First)
    expectMsg(Second)
    expectMsg(Last)
    expectMsg(Sequencer.Done)
  }
}


// vim: set ts=2 sw=2 et:
