package com.example.saga.impl

import java.util.UUID

import com.example.saga.api.{SagaExOneEvent, SagaResponse, SagaService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Implementation of the SagalagomService.
  */
class SagaServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends SagaService {

  lazy val log:Logger = LoggerFactory.getLogger(SagaService.getClass)


  override def getResult(id: String) = ServiceCall { _ =>
    // Look up the saga-lagom entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SagaEntity](id)

    // Ask the entity the Hello command.
    ref.ask(GetResult(id)).map(v => SagaResponse(id,v))
  }

  override def begin = ServiceCall { request =>
    // Look up the saga-lagom entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SagaEntity](request.id)

    // Tell the entity to use the greeting message specified.
    ref.ask(Begin(request.id))
  }

  override def sageExOneEvents(): Topic[SagaExOneEvent] = {
    TopicProducer.taggedStreamWithOffset(SagaEvent.Tag.allTags.to[immutable.Seq]) { (tag, offset) =>
      persistentEntityRegistry.eventStream(tag, offset).filter(e =>
        e.event.isInstanceOf[SagaBegun]
      ).mapAsync(1) { event =>
        event.event match {
          case SagaBegun(id: String) =>
            log.info(s"+++++++++++++++  sageExOneEvents $id ")
            Future.successful((SagaExOneEvent(id), event.offset))
        }

      }
    }
  }
}
