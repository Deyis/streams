package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import dtos.{BankSearchResult, BankUploadResult}
import io.swagger.annotations._
import javax.inject._
import models.Bank
import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart
import play.core.parsers.Multipart.FileInfo
import repositories.BankRepository

import scala.concurrent.ExecutionContext


@Singleton
@Api
class BanksController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem, banksRepo: BankRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) {
  implicit val sys: ActorSystem = actorSystem
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  @ApiOperation(
    httpMethod = "POST",
    value = "Import bank information into db",
    consumes = "multipart/form-data",
    response = classOf[BankUploadResult]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(value = "csv file", name = "csv", required = true, dataType = "file", paramType = "form")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "amount of uploaded entities", response = classOf[BankUploadResult]),
    new ApiResponse(code = 500, message = "internal server error")))
  def upload: Action[MultipartFormData[Int]] = {
    Action(parse.multipartFormData(handleFilePartUpload)) {
      request =>
        val count = request.body.files.map({ case FilePart(_, _, _, multipartUploadResult, _, _) => multipartUploadResult }).sum
        Ok(Json.toJson(BankUploadResult(count)))
    }
  }

  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "json representation of bank information", response = classOf[BankSearchResult]),
    new ApiResponse(code = 404, message = "bank wasn't found")))
  def find(@ApiParam(value = "bank identifier") identifier: String): Action[AnyContent] = Action.async(
    banksRepo.find(identifier).map({
      case Some(bank) => Ok(Json.toJson(BankSearchResult(bank.name, bank.identifier)))
      case None => NotFound
    })
  )

  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "utility method to delete all the entries in db")))
  def deleteAll: Action[AnyContent] = Action.async(
    banksRepo.deleteAll.map(_ => Ok)
  )

  // todo specify max file length
  private def handleFilePartUpload: Multipart.FilePartHandler[Int] = {
    case FileInfo(partName, filename, contentType, _) =>
      val accumulator = Accumulator.flatten(banksRepo.sink.map(Accumulator(_)))

      val parsing = Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), 1000, allowTruncation = true))
        .drop(1) // skip header
        .map(_.utf8String.trim.split(";") match {
          // todo it will cause intentional error if file format is broken
          //  but might be good to return to the user number of row in which it has occurred
          //  also possible to add here validation that id is integer
          //  but I'm not sure if it's the case for all the identifiers in test data
          case Array(name, id) => Bank(name = name, identifier = id)
        })

      accumulator.through(parsing).map(res => FilePart(partName, filename, contentType, res))
  }
}
