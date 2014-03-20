package com.ataraxer.patterns.akka

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.actor.Actor.Receive
import akka.pattern.{ask, pipe}

import org.apache.zookeeper.{ZooKeeper, CreateMode, Watcher, WatchedEvent}
import org.apache.zookeeper.Watcher.Event.KeeperState._
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.data.Stat

import AkkaApp._


object ZooKeeperProxy {
  def props(client: ActorRef, host: String) = Props(new ZooKeeperProxy(client, host))

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
}


class ZooKeeperProxy(client: ActorRef, host: String, sessionTimeout: Int = 1000)
    extends Actor with Stash with Spawner
{
  import ZooKeeperProxy._

  private val watcher = new Watcher {
    override def process(event: WatchedEvent) {
      event.getState match {
        case SyncConnected => self ! Connected
        case _ => unstashAll()
      }
    }
  }

  private val zk = new ZooKeeper(host, sessionTimeout, watcher)


  def receive = {
    case Connected => {
      unstashAll()
      context.become(active)
    }
    case msg => stash
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
