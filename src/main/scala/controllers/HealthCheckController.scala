package controllers

import io.swagger.annotations.{Api, ApiResponse, ApiResponses}
import javax.inject._
import play.api.mvc._


@Singleton
@Api
class HealthCheckController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Api is up and running")))
  def check: Action[AnyContent] = Action {
    Ok("App is ready")
  }
}
