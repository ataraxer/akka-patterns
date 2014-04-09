package com.ataraxer.patterns.test.akka

import org.scalatest._

import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import akka.actor.{ActorSystem, ActorRef}

import com.ataraxer.patterns.akka._

import scala.concurrent.duration._
import scala.util.Random


object CollectorSpec {
  case object Foo
  case object Bar
  case object Baz
  case class Content(value: String)

  val fooCount = 5
  val barCount = 10
  val bazCount = 15

  def generateMessages =
    Random.shuffle(
      List.fill(5)(Foo) ++
      List.fill(10)(Bar) ++
      List.fill(15)(Baz)
    )
}


class CollectorSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
    with CollectorSpawner
{
  import Collector._
  import CollectorSpec._

  val collector = TestActorRef[Collector]

  def this() = this(ActorSystem("collector-spec"))

  "A Collector" should "collect expected messages" in {
    val collector = collect(Foo, Bar)
    generateMessages foreach { collector ! _ }

    collector ! Flush

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar)
        result.count(_ == Foo) should be (fooCount)
        result.count(_ == Bar) should be (barCount)
      }
    }
  }


  it should "collect values extracted with partial function" in {
    val collector = collectPF {
      case Foo => Bar
      case Bar => Foo
      case Content(value) => value
    }

    generateMessages foreach { collector ! _ }
    collector ! Content("foobar")

    collector ! Flush

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar, "foobar")
        result.count(_ == Foo) should be (barCount)
        result.count(_ == Bar) should be (fooCount)
      }
    }
  }


  it should "collect expected messages untill required count is reached" in {
    val collector = collectCount(fooCount + barCount)(Foo, Bar)
    generateMessages foreach { collector ! _ }

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar)
        result.count(_ == Foo) should be (fooCount)
        result.count(_ == Bar) should be (barCount)
      }
    }
  }


  it should "collect extracted by PF values, " +
            "untill required count is reached" in {
    val collector = collectCountPF(fooCount + barCount + 1) {
      case Foo => Bar
      case Bar => Foo
      case Content(value) => value
    }

    generateMessages foreach { collector ! _ }
    collector ! Content("foobar")

    expectMsgPF(1.second) {
      case Collection(result) => {
        result should contain allOf (Foo, Bar, "foobar")
        result.count(_ == Foo) should be (barCount)
        result.count(_ == Bar) should be (fooCount)
      }
    }
  }
}

// vim: set ts=2 sw=2 et:
