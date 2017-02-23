package com.example.exone.impl


import akka.NotUsed
import akka.stream.scaladsl.Sink
import com.example.exone.api.{ExOneEvent, ExOneService}
import com.example.saga.api.{SagaExOneEvent, SagaRequest, SagaResponse, SagaService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.collection.immutable.Seq

class ExOneServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  var producerStub: ProducerStub[SagaExOneEvent] = _


  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
  ) { ctx =>
    new ExOneApplication(ctx) with LocalServiceLocator with TestTopicComponents{
      // (1) creates an in-memory topic and binds it to a producer stub
      val stubFactory = new ProducerStubFactory(actorSystem, materializer)
      producerStub =
        stubFactory.producer[SagaExOneEvent](SagaService.EXONE_TOPIC_NAME)

      // (2) Override the default Saga service with our service stub
      // which gets the producer stub injected
      override lazy val sagaService = new SagaServiceStub(producerStub)
    }
  }

  val client = server.serviceClient.implement[ExOneService]


  import server.materializer

  override protected def afterAll() = server.stop()

  "exone service" should {

    "emit event" in {


      // (3) produce a message in the stubbed topic via it's producer
      producerStub.send(SagaExOneEvent("1"))


      for {
        event: Seq[ExOneEvent] <- client.exOneEvents().subscribe.atMostOnceSource
          .take(1)
          .runWith(Sink.seq)
      } yield {
        event.size shouldBe 1
        event.head shouldBe ExOneEvent("DSF","SDF")
      }

    }
  }


}

// (2) a Service stub that will use the in-memoru topic bound to
// our producer stub
class SagaServiceStub(stub: ProducerStub[SagaExOneEvent])
  extends SagaService {
  override def sageExOneEvents(): Topic[SagaExOneEvent] = stub.topic

  override def getResult(id: String): ServiceCall[NotUsed, SagaResponse] = ???

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"id":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  override def begin: ServiceCall[SagaRequest, String] = ???
}
