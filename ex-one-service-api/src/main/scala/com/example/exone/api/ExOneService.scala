package com.example.exone.api

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

object ExOneService {

  val TOPIC_NAME = "exoneEvents"

}


trait ExOneService extends Service {

  def exOneEvents(): Topic[ExOneEvent]

  override final def descriptor = {
    import Service._
    named("exonedocs").withTopics(
      topic(ExOneService.TOPIC_NAME, exOneEvents)
    ).withAutoAcl(true)
  }

}

case class ExOneEvent(id:String, value:String) extends AggregateEvent[ExOneEvent]{
  override def aggregateTag: AggregateEventTagger[ExOneEvent] = ExOneEvent.Tag
}
object ExOneEvent{
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[ExOneEvent](4)
  implicit val format: Format[ExOneEvent] = Json.format[ExOneEvent]
}