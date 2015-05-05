package playground.akka.persistence

import akka.actor._
import akka.persistence._


object EventLogger {
  case object MakeSnapshot
  final case class Event(message: String)

  case object PrintState

  case class EventLoggerState(state: Seq[Event]) {
    def add(event: Event) = copy(state :+ event)
  }
}


class EventLogger extends PersistentActor {
  import EventLogger._

  override def persistenceId = "event-logger"

  private var state = EventLoggerState(Seq.empty[Event])

  def updateState(event: Event) = {
    state = state.add(event)
  }


  def receiveRecover = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: EventLoggerState) => state = snapshot
  }


  def receiveCommand = {
    case event: Event => persist(event)(updateState)
    case PrintState => println(state)
    case MakeSnapshot => saveSnapshot(state)
  }
}


object PersistenceApp extends App {
  import EventLogger._

  implicit val system = ActorSystem("demo-system")
  val logger = system actorOf Props[EventLogger]

  logger ! PrintState
  logger ! Event("one!")
  logger ! Event("two!")
  logger ! PrintState
}


// vim: set ts=2 sw=2 et sts=2:
