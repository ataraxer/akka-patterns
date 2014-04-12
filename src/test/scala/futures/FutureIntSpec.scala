package com.ataraxer.patterns.test.akka.futures

import org.scalatest._

import scala.concurrent.{ Await, Future, ExecutionContext }
import scala.concurrent.duration._
import scala.util.Success

import com.ataraxer.patterns.test.akka.UnitSpec
import com.ataraxer.patterns.akka.futures.FutureInt

import ExecutionContext.Implicits.global


object FutureIntSpecGlobals {
  val (x, y) = (9000, 42)
  val futureX = FutureInt { x }
  val futureY = FutureInt { y }
}


class FutureIntSpec extends UnitSpec {
  import FutureIntSpecGlobals._

  "A FutureInt" should "be able to be instanciated from future" in {
    FutureInt(Future { 9000 }) shouldBe a [FutureInt]
  }

  it should "be able to be instanciated with a block of code" in {
    (FutureInt { 9000 }) shouldBe a [FutureInt]
  }

  it should "be composable via + operator" in {
    val futureZ = futureX + futureY
    val result = Await.result(futureZ, 1.second)
    result should be (x + y)
  }

  it should "be composable via - operator" in {
    val futureZ = futureX - futureY
    val result = Await.result(futureZ, 1.second)
    result should be (x - y)
  }

  it should "be composable via * operator" in {
    val futureZ = futureX * futureY
    val result = Await.result(futureZ, 1.second)
    result should be (x * y)
  }

  it should "be composable via / operator" in {
    val futureZ = futureX / futureY
    val result = Await.result(futureZ, 1.second)
    result should be (x / y)
  }
}

// vim: set ts=2 sw=2 et:
