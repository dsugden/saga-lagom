package com.example.saga.impl

import java.time.LocalDateTime

import akka.Done
import com.example.saga.api.SagaResponse
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

/**
  * This is an event sourced entity. It has a state, [[SagaState]], which
  * stores what the greeting should be (eg, "Hello").
  *
  * Event sourced entities are interacted with by sending them commands. This
  * entity supports two commands, a [[Begin]] command, which is
  * used to change the greeting, and a [[GetResult]] command, which is a read
  * only command which returns a greeting to the name specified by the command.
  *
  * Commands get translated to events, and it's the events that get persisted by
  * the entity. Each event will have an event handler registered for it, and an
  * event handler simply applies an event to the current state. This will be done
  * when the event is first created, and it will also be done when the entity is
  * loaded from the database - each event will be replayed to recreate the state
  * of the entity.
  *
  * This entity defines one event, the [[SagaBegun]] event,
  * which is emitted when a [[Begin]] command is received.
  */
class SagaEntity extends PersistentEntity {

  override type Command = SagaCommand[_]
  override type Event = SagaEvent
  override type State = SagaState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: SagaState = SagaState("Hello", LocalDateTime.now.toString)

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case SagaState(message, _) => Actions().onCommand[Begin, String] {

      // Command handler for the UseGreetingMessage command
      case (Begin(id), ctx, state) =>
        // In response to this command, we want to first persist it as a
        // GreetingMessageChanged event
        ctx.thenPersist(
          SagaBegun(id)
        ) { _ =>
          // Then once the event is successfully persisted, we respond with done.
          ctx.reply(id)
        }

    }.onReadOnlyCommand[GetResult, String] {

      // Command handler for the Hello command
      case (GetResult(id), ctx, state) =>
        // Reply with a message built from the current message, and the name of
        // the person we're meant to say hello to.
        ctx.reply("x")

    }.onEvent {

      // Event handler for the GreetingMessageChanged event
      case (SagaBegun(newMessage), state) =>
        // We simply update the current state to use the greeting message from
        // the event.
        SagaState(newMessage, LocalDateTime.now().toString)

    }
  }
}

/**
  * The current state held by the persistent entity.
  */
case class SagaState(message: String, timestamp: String)

object SagaState {
  /**
    * Format for the hello state.
    *
    * Persisted entities get snapshotted every configured number of events. This
    * means the state gets stored to the database, so that when the entity gets
    * loaded, you don't need to replay all the events, just the ones since the
    * snapshot. Hence, a JSON format needs to be declared so that it can be
    * serialized and deserialized when storing to and from the database.
    */
  implicit val format: Format[SagaState] = Json.format
}

/**
  * This interface defines all the events that the SagalagomEntity supports.
  */
trait SagaEvent extends AggregateEvent[SagaEvent]{
  override def aggregateTag: AggregateEventTagger[SagaEvent] = SagaEvent.Tag
}

object SagaEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[SagaEvent](4)
}


/**
  * An event that represents a change in greeting message.
  */
case class SagaBegun(id: String) extends SagaEvent

object SagaBegun {

  /**
    * Format for the greeting message changed event.
    *
    * Events get stored and loaded from the database, hence a JSON format
    * needs to be declared so that they can be serialized and deserialized.
    */
  implicit val format: Format[SagaBegun] = Json.format
}

/**
  * This interface defines all the commands that the HelloWorld entity supports.
  */
sealed trait SagaCommand[R] extends ReplyType[R]

/**
  * A command to switch the greeting message.
  *
  * It has a reply type of [[Done]], which is sent back to the caller
  * when all the events emitted by this command are successfully persisted.
  */
case class Begin(id: String) extends SagaCommand[String]

object Begin {

  /**
    * Format for the use greeting message command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[Begin] = Json.format
}

/**
  * A command to say hello to someone using the current greeting message.
  *
  * The reply type is String, and will contain the message to say to that
  * person.
  */
case class GetResult(id:String) extends SagaCommand[String]

object GetResult {

  /**
    * Format for the hello command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[GetResult] = Json.format
}



/**
  * Akka serialization, used by both persistence and remoting, needs to have
  * serializers registered for every type serialized or deserialized. While it's
  * possible to use any serializer you want for Akka messages, out of the box
  * Lagom provides support for JSON, via this registry abstraction.
  *
  * The serializers are registered here, and then provided to Lagom in the
  * application loader.
  */
object SagaSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[Begin],
    JsonSerializer[GetResult],
    JsonSerializer[SagaResponse],
    JsonSerializer[SagaBegun],
    JsonSerializer[SagaState]
  )
}
