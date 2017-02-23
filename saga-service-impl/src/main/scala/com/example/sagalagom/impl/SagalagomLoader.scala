package com.example.sagalagom.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.example.sagalagom.api.SagalagomService
import com.softwaremill.macwire._

class SagalagomLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SagalagomApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SagalagomApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[SagalagomService]
  )
}

abstract class SagalagomApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[SagalagomService].to(wire[SagalagomServiceImpl])
  )

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = SagalagomSerializerRegistry

  // Register the saga-lagom persistent entity
  persistentEntityRegistry.register(wire[SagalagomEntity])
}
