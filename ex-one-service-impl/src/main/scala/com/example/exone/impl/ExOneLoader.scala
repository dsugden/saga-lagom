package com.example.exone.impl

import com.example.exone.api.ExOneService
import com.example.saga.api.SagaService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents

abstract class ExOneLoader(context: LagomApplicationContext) extends LagomApplication(context)
  with AhcWSComponents
  with LagomKafkaComponents {

  lazy val sagaService = serviceClient.implement[SagaService]

  override lazy val lagomServer = LagomServer.forServices(
    bindService[ExOneService].to(wire[ExOneServiceImpl])
  )
  override lazy val jsonSerializerRegistry = ExOneSerializer

  // Initialise everything
  wire[AuctionScheduler]
  wire[ItemServiceSubscriber]
}

class BiddingApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new BiddingApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext) =
    new BiddingApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[BiddingService]
  )
}
