package com.example.exone.impl

import com.example.exone.api.{ExOneEvent, ExOneService}
import com.example.saga.api.SagaService
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable

/**
  * Created by dave on 2017-02-23.
  */
class ExOneServiceImpl(sagaService: SagaService) extends ExOneService{


  lazy val log:Logger = LoggerFactory.getLogger(ExOneService.getClass)

  override def exOneEvents(): Topic[ExOneEvent] = {

    TopicProducer.taggedStreamWithOffset(ExOneEvent.Tag.allTags.to[immutable.Seq]){(tag,offset) =>
      sagaService.sageExOneEvents().subscribe.atMostOnceSource.map { ev =>
        log.info("++++++++++++++++++ exOneEvents" + ev.id)
        (ExOneEvent("DSF","SDF"),offset)

      }}



    }
}
