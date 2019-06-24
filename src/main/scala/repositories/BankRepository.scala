package repositories


import java.time.OffsetDateTime

import akka.Done
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import javax.inject.Inject
import models.Bank
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


class BankRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  implicit val session: SlickSession = SlickSession.forConfig(dbConfigProvider.get[JdbcProfile])

  private val MAIN_BANKS_TABLE_NAME = "banks"
  private val Banks = TableQuery[BanksTable]((tag:Tag) => new BanksTable(tag, MAIN_BANKS_TABLE_NAME))

  def deleteAll: Future[Unit] =
    db.run(Banks.schema.truncate)

  def find(identifier: String): Future[Option[Bank]] =
    db.run(Banks.filter(_.identifier === identifier).result.headOption)

  def sink: Future[Sink[Bank, Future[Int]]] = {
    db.run(Banks.schema.createIfNotExists) // todo add migration mechanism

    val temporalTableName = s"tmp${OffsetDateTime.now().toInstant.toEpochMilli}"
    val temporalTable = createTemporalTable(temporalTableName)
    val future = db.run(temporalTable.schema.create)

    val copyAndDeleteAction = (
        for {
          _ <- Banks.schema.truncate
          count <- copyInformation(temporalTable, Banks)
          _ <- temporalTable.schema.drop
        } yield count
      ).transactionally

    // copyAndDeleteAction performs only on success result so if something failed - we don't override last valid data
    // tmp table is not deleted in case of error for debug purposes
    val watcher = Flow[Bank].watchTermination()((_, f) => f.flatMap(_ => db.run(copyAndDeleteAction)))

    future.map(_ => {
      val sink: Sink[Bank, Future[Done]] = Slick.sink[Bank]((bank: Bank) => temporalTable += bank)
      watcher.toMat(sink)(Keep.left)
    })
  }

  private def copyInformation(temporalTable: TableQuery[BanksTable], generalTable: TableQuery[BanksTable]) =
    generalTable.forceInsertQuery(temporalTable)


  private def createTemporalTable(temporalTableName: String) =
    TableQuery[BanksTable]((tag:Tag) => new BanksTable(tag, temporalTableName))


  private class BanksTable(tag: Tag, tableName: String) extends Table[Bank](tag, tableName) {
    def identifier = column[String]("identifier", O.PrimaryKey)
    def name = column[String]("bank_name")

    def * = (name, identifier) <> (Bank.tupled, Bank.unapply)
  }
}
