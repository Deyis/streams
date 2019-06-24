package dtos

import play.api.libs.json._

case class BankUploadResult(count: Int)

object BankUploadResult {
  implicit val writes: OWrites[BankUploadResult] = Json.writes[BankUploadResult]
  implicit val reads: Reads[BankUploadResult] = Json.reads[BankUploadResult]
}