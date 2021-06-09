package controllers

import dtos.ProductCreateDTO
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import repositories.ProductRepository

import scala.concurrent.{ExecutionContext, Future}

class ProductController @Inject()(repo: ProductRepository,
                                  components: ControllerComponents,
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(components) {

  /**
   * Atempt to create a new product
   * based on dto from request body
   *
   * @return json representation of string message / id of the new product
   */
  def addProduct = Action(parse.json).async { implicit request =>
    request.body.validate[ProductCreateDTO].fold(
      errors => {
        Future.successful(
          BadRequest(Json.toJson("Product cannot be created"))
        )
      },
      product => {
        repo.create(product).map {
          case Left(message) => BadRequest(Json.toJson(message))
          case Right(value) => Ok(Json.toJson(s"New product with id [$value] is created"))
        }
      }
    )
  }

  /**
   * Atempt to view product by id
   * will increase views counter on the product
   *
   * @return json representation of string message / product
   */
  def getProduct(id: Long) = Action.async { _ =>
    repo.find(id).map {
      case Left(message) => BadRequest(Json.toJson(message))
      case Right(value) => Ok(Json.toJson(value))
    }
  }

  /**
   * Confusing method name - related to spec.
   * actually we do not delete products due to audit purposes
   * Set isArchive flag to true on a product with id
   *
   * @param id of a product
   * @return json representation of string message
   */
  def deleteProduct(id: Long) = Action.async { _ =>
    repo.archive(id).map {
      case Left(message) => BadRequest(Json.toJson(message))
      case Right(value) => Ok(Json.toJson(value))
    }
  }

  /**
   * List of products with views more than zero sorted by views desc and limited by count
   *
   * @param currency desired currency - if not found - USD is used
   * @param count    maximum limit length of the returned list
   * @return json representation of Seq of ProductViewDTO
   *         name: String,
   *         price: BigDecimal,
   *         currency: String,
   *         description: Option[String]
   */
  def getProducts(currency: String, count: Int) = Action.async { implicit request =>
    repo.mostUsedWithCurrency(currency.toUpperCase, count).map { products =>
      Ok(Json.toJson(products))
    }
  }
}
