package com.ataraxer.patterns.test.akka

import org.scalatest._

import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import akka.actor.{ActorSystem}

import com.ataraxer.patterns.akka._

import scala.concurrent.duration._


object AggregatorSpec {
  val input  = List(1, 2, 3)
}


class AggregatorSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
{
  import AggregatorSpec._

  val aggregator = TestActorRef[Aggregator]

  def this() = this(ActorSystem("aggregator-spec"))

  "An Aggregator" should "perform multiple actions and aggregate result" in {
    aggregator ! Aggregator.PerformList(input)
    expectMsgPF(3 seconds) {
      case Aggregator.ResultList(output) => {
        output should contain allOf (2, 4, 6)
      }
    }
  }
}

// vim: set ts=2 sw=2 et:
