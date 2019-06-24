package utils

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import dtos.{BankSearchResult, BankUploadResult}
import org.scalatest.ConfigMap
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.WsTestClient
import utils.BanksApiClient.ExternalCallException

import scala.concurrent.{ExecutionContext, Future}

class BanksApiClient(ws: WSClient, baseUrl: String) {
  def upload(body: Source[ByteString, NotUsed])(implicit ec: ExecutionContext): Future[BankUploadResult]  = {
    ws.url(s"http://$baseUrl/banks")
      .post(Source(FilePart("customers", "hello.csv", Option("text/plain"), body) :: List())).flatMap({
      case resp if resp.status == 200 => Future.successful(Json.parse(resp.body).as[BankUploadResult])
      case resp => Future.failed(new ExternalCallException(resp.status, resp.body))
    })
  }

  def find(bankId: String)(implicit ec: ExecutionContext): Future[Option[BankSearchResult]]  = {
    ws.url(s"http://$baseUrl/banks/$bankId").get().map({
      case resp if resp.status == 200 => Json.parse(resp.body).asOpt[BankSearchResult]
      case _ => None
    })
  }

  def deleteAll(implicit ec: ExecutionContext): Future[Unit]  = {
    ws.url(s"http://$baseUrl/internal/banks").delete().flatMap({
      case resp if resp.status == 200 => Future.successful(())
      case resp => Future.failed(new ExternalCallException(resp.status, resp.body))
    })
  }
}

object BanksApiClient {
  class ExternalCallException(status: Int, message: String) extends RuntimeException

  def withBankApiClient(configMap: ConfigMap)(test: BanksApiClient => Any): Any = {
    WsTestClient.withClient(ws => {
      val client = new BanksApiClient(ws, DockerTestUtils.getAppHost(configMap))
      test(client)
    })
  }
}