package com.example.exone.api

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.broker.Topic

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

case class ExOneRequest(id:String)

case class ExOneEvent(id:String, value:String)