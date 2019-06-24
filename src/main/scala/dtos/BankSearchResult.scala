package dtos
import play.api.libs.json._

case class BankSearchResult(bankName:String, bankId: String)

object BankSearchResult {
  implicit val writes: OWrites[BankSearchResult] = Json.writes[BankSearchResult]
  implicit val reads: Reads[BankSearchResult] = Json.reads[BankSearchResult]
}