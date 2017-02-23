package com.example.exone.impl

import com.example.exone.api.{ExOneEvent, ExOneService}
import com.example.saga.api.SagaService
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer

/**
  * Created by dave on 2017-02-23.
  */
class ExOneServiceImpl(sagaService: SagaService) extends ExOneService{
  override def exOneEvents(): Topic[ExOneEvent] = {

    TopicProducer.singleStreamWithOffset{offset =>
      sagaService.sageExOneEvents().subscribe.atMostOnceSource.map { ev =>

        (ExOneEvent("DSF","SDF"),offset)

      }}



    }
}
