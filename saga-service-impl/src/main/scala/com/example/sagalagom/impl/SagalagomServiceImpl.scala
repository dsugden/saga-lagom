package com.example.sagalagom.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.example.sagalagom.api.SagalagomService

/**
  * Implementation of the SagalagomService.
  */
class SagalagomServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends SagalagomService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the saga-lagom entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SagalagomEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id, None))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the saga-lagom entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SagalagomEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }
}
