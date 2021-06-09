package dtos

import play.api.libs.json.Json

case class ProductCreateDTO(name: String,
                            price: BigDecimal,
                            description: Option[String] = None)

object ProductCreateDTO {
  implicit val format = Json.format[ProductCreateDTO]
}
