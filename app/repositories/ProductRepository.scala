package repositories

import dtos.{ProductCreateDTO, ProductViewDTO}
import javax.inject.{Inject, Singleton}
import models.{Product, Rate}
import play.api.libs.ws.WSClient
import slick.jdbc.SQLiteProfile.api._
import utils.App

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductRepository @Inject()()(implicit ec: ExecutionContext, ws: WSClient) {

  private class ProductTable(tag: Tag) extends Table[Product](tag, "products") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def price = column[BigDecimal]("price")

    def description = column[Option[String]]("description")

    def views = column[Int]("views")

    def isArchive = column[Boolean]("isArchive")

    def * = (name, price, description, views, isArchive, id) <> ((Product.apply _).tupled, Product.unapply)
  }

  /**
   * Simple check of database connection availobility
   *
   * @return 1
   */
  def checkDatabaseConnection(): Future[Int] = App.db.run {
    sql"""select 1""".as[Int].head
  }

  private val products = TableQuery[ProductTable]

  /**
   * Create a new product
   * dto has to pass few checks:
   * - name has to be not empty/blank
   * - price has to be more than zero (normal price) or zero (gift)
   *
   * @param dto name: String,
   *            price: BigDecimal,
   *            description: Option[String] = None
   * @return Left - error message
   *         Right - id of new product
   */
  def create(dto: ProductCreateDTO): Future[Either[String, Long]] = {
    if (dto.name.trim.isEmpty)
      Future.successful(Left("Name has to be filled"))
    else if (dto.price < 0)
      Future.successful(Left("Price must be zero or positive"))
    else
      App.db.run {
        (products returning products.map(_.id)) += Product.from(dto)
      }.map(r => Right(r))
  }

  /**
   * Set isArchive flag to true on a product with id
   *
   * @param id of a product
   * @return Left - error message
   *         Right - successful message
   */
  def archive(id: Long): Future[Either[String, String]] = App.db.run {
    for {
      _ <- products.filter(_.id === id).map(_.isArchive).update(true)
    } yield Right("Archived")
  }.recover { case _ => Left("Id not found") }

  /**
   * Display product by id
   * This method has side-effect:
   * each execution increase views counter on one for the choosen product
   *
   * @param id of a product
   * @return Left - error message
   *         Right - ProductViewDTO
   *         name: String,
   *         price: BigDecimal,
   *         currency: String,
   *         description: Option[String]
   */
  def find(id: Long): Future[Either[String, ProductViewDTO]] = App.db.run {
    for {
      product <- products.filter(_.id === id).result.head
      _ <- products.filter(_.id === id).map(_.views).update(product.views + 1)
    } yield {
      Right(ProductViewDTO.from(product))
    }
  }.recover { case _ => Left("Id not found") }

  /**
   * Search currency rate
   *
   * @param currency string abbreviature of the currency title
   * @return Option Rate
   *         currency: String,
   *         rate: BigDecimal
   */
  def checkCurrency(currency: String): Future[Option[Rate]] = {
    val a = ws.url("http://api.currencylayer.com/live").addQueryStringParameters(
      "access_key" -> App.config.getString("currencylayer.key"),
      "format" -> "1",
    ).get()

    a.map(response => (response.json \ "quotes").validate[Map[String, BigDecimal]].fold(
      errors => {
        None
      },
      quotes => {
        val key = quotes.keySet.filter(rate => rate.endsWith(currency)).headOption
        key.map(k => Rate(currency, quotes(k)))
      }
    ))
  }

  /**
   * List of products with views more than zero sorted by views desc and limited by count
   *
   * @param currency desired currency - if not found - USD is used
   * @param count    maximum limit length of the returned list
   * @return Seq of ProductViewDTO
   *         name: String,
   *         price: BigDecimal,
   *         currency: String,
   *         description: Option[String]
   */
  def mostUsedWithCurrency(currency: String, count: Int): Future[Seq[ProductViewDTO]] = {
    for {
      currencyRate <- checkCurrency(currency)
      topProducts <- App.db.run {
        products.filterNot(_.isArchive)
          .filter(_.views > 0)
          .sortBy(_.views.desc)
          .take(count)
          .result
      }.map { p =>
        p.map(p => ProductViewDTO.from(p, currencyRate))
      }
    } yield {
      topProducts
    }
  }
}
