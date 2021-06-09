package dtos

import models.{Product, Rate}
import play.api.libs.json.Json

case class ProductViewDTO(name: String,
                          price: BigDecimal,
                          currency: String = "USD",
                          description: Option[String] = None)

object ProductViewDTO {
  implicit val format = Json.format[ProductViewDTO]

  def from(product: Product, rate: Option[Rate] = None): ProductViewDTO = {
    val currencyRate = rate match {
      case Some(value) => value
      case None => Rate("USD", 1)
    }
    ProductViewDTO(
      name = product.name,
      price = product.price * currencyRate.rate,
      currency = currencyRate.currency,
      description = product.description)
  }
}
