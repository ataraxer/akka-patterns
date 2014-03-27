package com.ataraxer.patterns.test.akka

import org.scalatest._

import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import akka.actor.{ActorSystem}

import com.ataraxer.patterns.akka._


object SequencerSpec {
  val printSeq = List(1, 2, 3, 4)
}


class SequencerSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
{
  import SequencerSpec._

  val sequencer = TestActorRef[Sequencer]

  def this() = this(ActorSystem("sequencer-spec"))

  "A Sequencer" should "perform actions sequntially" in {
    sequencer ! Sequencer.Perform(printSeq)
    expectMsg(Sequencer.Done)
  }
}

// vim: set ts=2 sw=2 et:
