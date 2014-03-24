package com.ataraxer.patterns.akka.futures

import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._


object FutureInt {
  implicit def futureToFutureInt(future: Future[Int])(implicit ec: ExecutionContext): FutureInt =
    new FutureInt(future)

  implicit def futureIntToFuture(futureInt: FutureInt): Future[Int] =
    futureInt.future

  def apply(body: => Int)(implicit ec: ExecutionContext): FutureInt =
    new FutureInt( Future { body } )

  def apply(future: Future[Int])(implicit ec: ExecutionContext): FutureInt =
    new FutureInt(future)
}

class FutureInt(val future: Future[Int])(implicit ex: ExecutionContext) {
  def +(that: FutureInt) =
    for (a <- future; b <- that)
      yield a + b
}


object FutureIntApp extends Application {
  import FutureInt._

  implicit val ex: ExecutionContext = ExecutionContext.global

  val futureA = Future { 42 }
  val futureB = Future { 9000 }

  futureA.onComplete(println)
  futureB.onComplete(println)

  val futureC = futureA + futureB

  futureC.onComplete(println)

  val result = Await.result(futureC, 1 second)
  println(result)
}


// vim: set ts=2 sw=2 et:
