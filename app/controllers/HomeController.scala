package controllers

import javax.inject._
import play.api.mvc._
import utils.App

import scala.concurrent.ExecutionContext

class HomeController @Inject()(components: ControllerComponents
                              )(implicit ec: ExecutionContext)
  extends AbstractController(components) {

  /**
   * base index with db check and link to the swagger
   */
  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    App.checkDatabaseConnection().map { r =>
      Ok(s"Check DB: $r\nSwagger: /docs")
    }
  }

  /**
   * Convenient shortcut to swagger-ui page
   */
  def redirectDocs: Action[AnyContent] = Action {
    Redirect(
      url = "/docs/swagger-ui/index.html",
      queryStringParams = Map("url" -> Seq("/assets/swagger.json")))
  }
}
