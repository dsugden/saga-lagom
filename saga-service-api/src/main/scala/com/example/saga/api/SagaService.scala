package com.example.saga.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}


object SagaService{
  val EXONE_TOPIC_NAME = "sagaExOneEvents"
}

trait SagaService extends Service {


  def sageExOneEvents(): Topic[SagaExOneEvent]

  def getResult(id: String): ServiceCall[NotUsed, SagaResponse]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"id":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def begin: ServiceCall[SagaRequest, String]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("saga-lagom").withCalls(
      pathCall("/api/saga/:id", getResult _),
      pathCall("/api/saga", begin _)
    ).withTopics(
      topic(SagaService.EXONE_TOPIC_NAME, sageExOneEvents)
    ).withAutoAcl(true)
    // @formatter:on
  }
}

case class SagaRequest(id: String)
object SagaRequest {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[SagaRequest] = Json.format[SagaRequest]
}


case class SagaResponse(id: String, value:String)
object SagaResponse {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[SagaResponse] = Json.format[SagaResponse]
}


case class SagaExOneEvent(id: String)
object SagaExOneEvent {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[SagaExOneEvent] = Json.format[SagaExOneEvent]
}
