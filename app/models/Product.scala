package models

import dtos.ProductCreateDTO

/**
 * Base product entity
 * @param name
 * @param price
 * @param description
 * @param views number of times when product has been viewed
 * @param isArchive trigger to show product in list
 * @param id
 */
case class Product(name: String,
                   price: BigDecimal,
                   description: Option[String] = None,
                   views: Int = 0,
                   isArchive: Boolean = false,
                   id: Long = 0)

object Product {
  def from(dto: ProductCreateDTO): Product = Product(dto.name, dto.price, dto.description)
}

