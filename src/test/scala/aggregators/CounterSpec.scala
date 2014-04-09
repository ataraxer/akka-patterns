package com.ataraxer.patterns.test.akka

import org.scalatest._

import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import akka.actor.{ActorSystem, ActorRef}

import com.ataraxer.patterns.akka._

import scala.concurrent.duration._
import scala.util.Random


object CounterSpec {
  case object Foo
  case object Bar
  case object Baz
}


class CounterSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
    with CounterSpawner
{
  import Counter._
  import CounterSpec._

  val counter = TestActorRef[Counter]

  def this() = this(ActorSystem("counter-spec"))

  "A Counter" should "count expected messages" in {
    val counterA = countPF {
      case Foo =>
      case Bar =>
    }
    val counterB = count(Foo.getClass, Bar.getClass)

    val messages = Random.shuffle(
      List.fill(5)(Foo) ++
      List.fill(10)(Bar) ++
      List.fill(15)(Baz)
    )

    messages foreach { counterA ! _ }
    messages foreach { counterB ! _ }

    counterA ! Flush

    expectMsgPF(1 seconds) {
      case Count(result) =>
        result should be { Map(Foo -> 5, Bar -> 10) }
    }

    counterB ! Flush

    expectMsgPF(1 seconds) {
      case Count(result) =>
        result should be { Map(Foo -> 5, Bar -> 10) }
    }

    val counterC: ActorRef = count[String] + count[List[Int]]
    counterC ! "foo"
    counterC ! "foo"
    counterC ! List(1, 2, 3)
    counterC ! "bar"
    counterC ! "foo"
    counterC ! Flush

    expectMsg(Count(
      Map("foo" -> 3, "bar" -> 1, List(1, 2, 3) -> 1)
    ))

  }
}

// vim: set ts=2 sw=2 et:
