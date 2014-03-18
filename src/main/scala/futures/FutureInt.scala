package com.ataraxer.patterns.akka.futures

import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._


object FutureInt {
  implicit def futureToFutureInt(future: Future[Int]): FutureInt =
    FutureInt(future)

  implicit def futureIntToFuture(futureInt: FutureInt): Future[Int] =
    futureInt.future
}

case class FutureInt(future: Future[Int]) {
  implicit val ex: ExecutionContext = ExecutionContext.global

  def +(that: Future[Int]) =
    for (a <- future; b <- that)
      yield a + b
}


object FutureIntApp extends Application {
  import FutureInt._

  implicit val ex: ExecutionContext = ExecutionContext.global

  val futureA = Future { 42 }
  val futureB = Future { 9000 }

  val futureC = futureA + futureB

  val result = Await.result(futureC, 1 second)
  println(result)
}


// vim: set ts=2 sw=2 et:
