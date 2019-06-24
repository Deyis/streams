package controllers

import org.scalatest._
import org.scalatest.concurrent._
import utils.{DockerComposeTestTag, HealthCheckApiClient}

class HealthCheckControllerSpec extends fixture.FunSuite with fixture.ConfigMapFixture with Eventually with ScalaFutures with IntegrationPatience with Matchers {

  test("validate that health check endpoint returns a success response", DockerComposeTestTag) {
    HealthCheckApiClient.withHealthCheckApiClient(_) { client =>
      eventually {
        val res = client.check.futureValue
        res.status shouldEqual  200
        res.body shouldEqual "App is ready"
      }
    }
  }
}
