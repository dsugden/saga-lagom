package com.example.saga.impl

import java.util.UUID

import akka.stream.scaladsl.Sink
import com.example.saga.api.{SagaExOneEvent, SagaRequest, SagaResponse, SagaService}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.collection.immutable.Seq

class SagaServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new SagaApplicationBase(ctx) with LocalServiceLocator with TestTopicComponents
  }

  val client = server.serviceClient.implement[SagaService]


  import server.materializer

  override protected def afterAll() = server.stop()

  "saga service" should {

    "get Result" in {
      client.getResult("1").invoke().map { answer =>
        answer should ===(SagaResponse("1","x"))
      }
    }

    "return saga id" in {
      for {
        _ <- client.begin.invoke(SagaRequest("100"))
        answer <- client.getResult("100").invoke()
      } yield {
        answer should ===(SagaResponse("100","x"))
      }
    }
  }

  "saga service should publish event" in {
      for {
        _ <- client.begin.invoke(SagaRequest("100"))
        event: Seq[SagaExOneEvent] <- client.sageExOneEvents().subscribe.atMostOnceSource
          .take(1)
          .runWith(Sink.seq)
      } yield {
        event.size shouldBe 1
        event.head shouldBe SagaExOneEvent("100")
      }
  }


}
