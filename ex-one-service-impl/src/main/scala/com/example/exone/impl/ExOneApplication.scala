package com.example.exone.impl

import com.example.exone.api.ExOneService
import com.example.saga.api.SagaService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents

abstract class ExOneApplication(context: LagomApplicationContext) extends ExOneApplicationBase(context)
  with LagomKafkaComponents



abstract class ExOneApplicationBase(context: LagomApplicationContext) extends LagomApplication(context)
  with AhcWSComponents
  with CassandraPersistenceComponents{

  lazy val sagaService = serviceClient.implement[SagaService]

  override lazy val lagomServer = LagomServer.forServices(
    bindService[ExOneService].to(wire[ExOneServiceImpl])
  )
  override lazy val jsonSerializerRegistry = ExOneSerializer
}




class ExOneLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ExOneApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ExOneApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[SagaService]
  )
}
