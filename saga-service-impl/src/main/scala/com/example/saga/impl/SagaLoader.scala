package com.example.saga.impl

import com.example.saga.api.SagaService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._

class SagaLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SagaApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SagaApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[SagaService]
  )
}

abstract class SagaApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  lazy val sagaService = serviceClient.implement[SagaService]


  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[SagaService].to(wire[SagaServiceImpl])
  )

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = SagaSerializerRegistry

  // Register the saga-lagom persistent entity
  persistentEntityRegistry.register(wire[SagaEntity])
}
