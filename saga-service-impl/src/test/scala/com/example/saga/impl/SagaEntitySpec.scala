package com.example.saga.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class SagaEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("SagalagomEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(SagaSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[SagaCommand[_], SagaEvent, SagaState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new SagaEntity, "saga-lagom-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "saga-lagom entity" should {

    "get result" in withTestDriver { driver =>
      val outcome = driver.run(GetResult("1"))
      outcome.replies should contain only "x"
    }

    "begin saga, get saga id" in withTestDriver { driver =>
      val outcome1 = driver.run(Begin("2"))
      outcome1.events should contain only SagaBegun("2")
    }

  }
}
