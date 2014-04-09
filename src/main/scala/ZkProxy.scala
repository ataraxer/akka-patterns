package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.actor.Actor.Receive
import akka.actor.ActorLogging
import akka.pattern.{ask, pipe}

import org.apache.zookeeper.{ZooKeeper, CreateMode, Watcher, WatchedEvent}
import org.apache.zookeeper.Watcher.Event.KeeperState._
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.data.Stat

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import AkkaApp._


object ZooKeeperProxy {
  def props(client: ActorRef, host: String) = Props(new ZooKeeperProxy(client, host))

  val ConnectionTimeout = 5 seconds

  /* ==== MESSAGES ==== */
  case class Close
  case class Connected

  case class Create(path: String, data: Option[String])
  case class CreateRecursive(path: String, data: Option[String])

  case class Data(data: String)
  case class Delete(path: String)

  case class Exists(path: String)

  case class GetChildren(path: String)
  case class GetData(path: String)

  case class SetData(path: String, data: String)

  case class Timeout

  case class Ping(message: String)

  /* ==== EXCEPTIONS ==== */
  class ZkProxySessionExpired extends Exception
}


class ZooKeeperProxy(
  client: ActorRef, host: String,
  sessionTimeout: Int = 30.seconds.toMillis.toInt)
    extends Actor with Stash with Spawner with ActorLogging
{
  import ZooKeeperProxy._

  private val watcher = new Watcher {
    override def process(event: WatchedEvent) {
      event.getState match {
        case SyncConnected => self ! Connected
        case AuthFailed => {
          log.warning("ZK authentication failed!")
          reconnect()
        }
        case Disconnected => {
          log.warning("ZK has been disconnected!")
          reconnect()
        }
        case Expired => {
          log.warning("ZK session has expired!")
          startNewSession()
        }
        case _ => log.warning("Unhandled ZK event: {}", event)
      }
    }
  }

  private var zk: ZooKeeper = null
  startNewSession()

  def startNewSession() {
    zk = new ZooKeeper(host, sessionTimeout, watcher)
    log.info("Starting new ZK session...")
    connect()
  }


  def connect() {
    log.info("Connecting to ZK...")
    context.become(connecting)
    context.system.scheduler.scheduleOnce(ConnectionTimeout, self, Timeout)
  }


  def reconnect() {
    log.info("Attempting to reconnect to ZK...")
    connect()
  }


  def receive = connecting


  def connecting: Receive = {
    case Connected => {
      unstashAll()
      context.become(active)
    }
    case Timeout => {
      log.warning("Connection timed out!")
      reconnect()
    }
    case msg if active.isDefinedAt(msg) => stash
  }


  def active: Receive = {
    case Close => {
      zk.close()
      context.stop(self)
    }

    case Create(path, data) => {
      val byteData = data.map(_.getBytes("utf8")).getOrElse(null)
      zk.create(path, byteData, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
      sender ! Shutdown
    }

    case Exists(path) => {
      val response = zk.exists(path, false)
      val exists = (!Option(response).isEmpty)
      sender ! exists
    }

    case GetData(path) => {
      val data = zk.getData(path, false, null)
      sender ! Data(new String(data))
    }

    case Ping(msg) => log.info("Ping: {}", msg)
  }


  def dead: Receive = {
    case _ => sender ! Shutdown
  }
}


object ZooKeeperProxyApp extends AkkaApp("zk-proxy-app") {
  def run {
    val zkProxy = system.actorOf(ZooKeeperProxy.props(worker, "localhost:2181"))
    val path = "/a"
    val data = "foobar"

    zkProxy ! ZooKeeperProxy.Create(path, Some(data))
  }
}


// vim: set ts=2 sw=2 et:
