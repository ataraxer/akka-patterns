package com.ataraxer.patterns.test.akka.futures

import org.scalatest._

import scala.concurrent.{ Await, Future, ExecutionContext }
import scala.concurrent.duration._
import scala.util.Success

import com.ataraxer.patterns.test.akka.UnitSpec
import com.ataraxer.patterns.akka.futures.FutureInt


class FutureIntSpec extends UnitSpec {
  import ExecutionContext.Implicits.global

  "A FutureInt" should "be able to be instanciated from future" in {
    FutureInt(Future { 9000 }) shouldBe a [FutureInt]
  }

  it should "be able to be instanciated with a block of code" in {
    (FutureInt { 9000 }) shouldBe a [FutureInt]
  }

  it should "be composable via + operator" in {
    val (a, b) = (9000, 42)
    val futureA = FutureInt { a }
    val futureB = FutureInt { b }
    val futureC = futureA + futureB

    val result = Await.result(futureC, 1 second)
    result should be (a + b)
  }
}

// vim: set ts=2 sw=2 et:
