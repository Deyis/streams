package utils

import org.scalatest.ConfigMap
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.WsTestClient

import scala.concurrent.Future

class HealthCheckApiClient(ws: WSClient, baseUrl: String) {
  def check: Future[WSResponse] = {
    ws.url(s"http://$baseUrl/check").get()
  }
}

object HealthCheckApiClient {
  def withHealthCheckApiClient(configMap: ConfigMap)(test: HealthCheckApiClient => Any): Any = {
    WsTestClient.withClient(ws => {
      val client = new HealthCheckApiClient(ws, DockerTestUtils.getAppHost(configMap))
      test(client)
    })
  }
}