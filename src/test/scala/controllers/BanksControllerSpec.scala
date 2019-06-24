package controllers

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import dtos.BankSearchResult
import org.scalatest._
import org.scalatest.concurrent._
import utils.{BanksApiClient, DockerComposeTestTag}

import scala.concurrent.ExecutionContext.Implicits.global

class BanksControllerSpec extends fixture.FunSuite with fixture.ConfigMapFixture with Eventually with ScalaFutures with IntegrationPatience with Matchers with BeforeAndAfterEachTestData {

  override def afterEach(testData: TestData) {
    BanksApiClient.withBankApiClient(testData.configMap) { client =>
      client.deleteAll.futureValue
    }
  }

  test("file upload endpoint returns a success response", DockerComposeTestTag){
    BanksApiClient.withBankApiClient(_) { client =>
      val body = generateCsv(List("asd" -> "123", "nsdgsdg" -> "314")) // todo use ScalaCheck to create random values
      eventually {
        val res = client.upload(body).futureValue
        res.count shouldEqual 2
      }
    }
  }

  test("after success upload it should find bank by id", DockerComposeTestTag) {
    BanksApiClient.withBankApiClient(_) { client =>
      val body = generateCsv(List("asd" -> "123", "nsdgsdg" -> "314"))
      eventually {
        client.upload(body).futureValue
        val res = client.find("314").futureValue
        res shouldBe defined
        res.get shouldEqual BankSearchResult("nsdgsdg", "314")
      }
    }
  }

  test("successful upload should delete old values", DockerComposeTestTag) {
    BanksApiClient.withBankApiClient(_) { client =>
      val body = generateCsv(List("asd" -> "123", "nsdgsdg" -> "314"))
      val body2 = generateCsv(List("asd2" -> "123", "nsdgsdg2" -> "3142"))
      eventually {
        client.upload(body).futureValue
        client.upload(body2).futureValue
        val res = client.find("314").futureValue
        res shouldBe empty
      }
    }
  }

  test("if upload fails - nothing from new file should be found", DockerComposeTestTag) {
    BanksApiClient.withBankApiClient(_) { client =>
      val body = generateBrokenCsv(List("asd" -> "123", "nsdgsdg" -> "314"))
      eventually {
        client.upload(body).failed.futureValue
        val res = client.find("314").futureValue
        res shouldBe empty
      }
    }
  }

  test("if upload fails - old data should be still available", DockerComposeTestTag) {
    BanksApiClient.withBankApiClient(_) { client =>
      val body = generateCsv(List("qwwe1" -> "123423", "nsdwegewwrgsdg1" -> "31454"))
      val brokenBody = generateBrokenCsv(List("asd2" -> "123", "nsdgsdg2" -> "314"))
      eventually {
        client.upload(body).futureValue
        client.upload(brokenBody).failed.futureValue
        val res = client.find("31454").futureValue
        res shouldBe defined
        res.get shouldEqual BankSearchResult("nsdwegewwrgsdg1", "31454")
      }
    }
  }

  // todo use proper library for scv body generation
  def generateCsv(list: List[(String, String)]): Source[ByteString, NotUsed] =
      Source(
        ByteString((("name" -> "bank_identifier") :: list)
          .map({case (name, id) => s"$name;$id"})
          .mkString("\n")
        ) :: List()
      )

  def generateBrokenCsv(list: List[(String, String)]): Source[ByteString, NotUsed] = {
    val brokenData = (("name" -> "bank_identifier") :: list)
      .map({case (name, id) => s"$name;$id"}) ++
      List("broken row")
    Source(ByteString(brokenData.mkString("\n")) :: List())
  }
}