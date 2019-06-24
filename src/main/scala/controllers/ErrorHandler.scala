package controllers

import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.Singleton
import play.api.Logger

/**
  * Global error handler to prevent user seeing some internal details
  */
@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Logger("ErrorHandler").error(s"A client error occurred: $statusCode, $message")
    Future.successful(Status(statusCode)(s"A client error occurred: $message"))
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger("ErrorHandler").error(s"A server error occurred", exception)
    Future.successful(InternalServerError("A server error occurred"))
  }
}