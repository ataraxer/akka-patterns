package com.ataraxer.patterns.test.akka

import org.scalatest._

import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import akka.actor.{ActorSystem, Actor, ActorRef}
import akka.util.Timeout

import com.ataraxer.patterns.akka.Spawner

import scala.concurrent.duration._


object SpawnerSpec {
  case object Work
  case object Ping
  case object Done

  class SpawnerActor extends Actor with Spawner {
    val echoer = spawn(new Actor {
      def receive = {
        case msg => sender ! msg
      }
    })

    def receive = {
      case Work => {
        val client = sender

        val handler = handle {
          case Ping => client ! Done
        }

        echoer.tell(Ping, handler)
      }
    }
  }
}


class SpawnerSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with UnitSpec
{
  import SpawnerSpec._

  val spawner = TestActorRef[SpawnerActor]

  def this() = this(ActorSystem("spawner-spec"))

  "A Spawner" should "be able to spawn anonymous actors" in {
    spawner.underlyingActor.echoer shouldBe an[ActorRef]
  }

  it should "be able to spawn anonymous handlers" in {
    spawner ! Work
    expectMsg(Done)
  }
}

// vim: set ts=2 sw=2 et:
