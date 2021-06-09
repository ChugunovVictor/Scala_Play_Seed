package models

/**
 * Representation of currency cross course to USD
 * @param currency title abbreviature
 * @param rate multiplier
 */
case class Rate(currency: String,
                rate: BigDecimal)

import play.api.libs.json._

object Rate{
  implicit val rateReads = Json.reads[Rate]
}






