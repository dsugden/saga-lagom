package com.example.exone.impl

import com.example.exone.api.ExOneEvent
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

object ExOneSerializer extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[ExOneEvent]
  )
}
