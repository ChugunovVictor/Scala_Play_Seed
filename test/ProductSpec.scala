import dtos.ProductCreateDTO
import org.junit.runner._
import org.specs2.control.Properties.aProperty
import org.specs2.matcher.Matchers.beEmpty
import org.specs2.mutable._
import org.specs2.runner._
import play.api.http.Status.OK
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test._

import scala.Right
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Right

@RunWith(classOf[JUnitRunner])
class ProductSpec extends Specification {

  var createdIds = ArrayBuffer[Long]()

  /*
    TODO

    val controller = app.injector.instanceOf[controllers.ProductController]
    val result = controller.addProduct()(
        FakeRequest("POST", "/products")
          .withHeaders("Content-Type" -> "application/json")
          .withJsonBody( JsObject(
            Seq(
              "name" -> JsString("Test_1"),
              "price" -> JsNumber(1000),
              "description" -> JsString("Test_1_Description"),
           ))))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must contain("is created")

      import scala.util.matching.Regex
      val pattern: Regex = "\\[([0-9]+)\\]".r
      val id = pattern.findFirstMatchIn(result.toString)

      id must not be None
      if (id isDefined)
      createdIds += id.get.group(1).toLong
    */

  "Product" should {
    "be created with correct name/price/description" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      val result = repo.create(ProductCreateDTO("1",1000, Some("1")))
      val id = Await.result(result, Duration.Inf);
      id must beRight
      createdIds += id.right.get
    }

    "be created with correct name/price" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      val result = repo.create(ProductCreateDTO("2",2000))
      val id = Await.result(result, Duration.Inf);
      id must beRight
      createdIds += id.right.get
    }

    "be not created with empty name" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      val result = repo.create(ProductCreateDTO("",3000))
      val id = Await.result(result, Duration.Inf);
      id must beLeft
    }

    "be found" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      val result = repo.find(createdIds.head)
      val id = Await.result(result, Duration.Inf);
      id must beRight
    }

    "be not found" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      val result = repo.find(-1)
      val id = Await.result(result, Duration.Inf);
      id must beLeft
    }

    "be/not listed" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      val result = repo.mostUsedWithCurrency("USD", 2)
      val id = Await.result(result, Duration.Inf);
      id.map(_.name) must contain("1")
      id.map(_.name) must not contain("2")
    }

    "be/not listed" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      Await.result(repo.find(createdIds.last), Duration.Inf);

      val result = repo.mostUsedWithCurrency("CAD", 1)
      val id = Await.result(result, Duration.Inf);
      id.length must be equalTo (1)
      id(0).price.toInt must be_>(1000)
    }

    "be archived" in new WithApplication {
      val repo = app.injector.instanceOf[repositories.ProductRepository]
      createdIds.foreach(v => Await.result(repo.archive(v), Duration.Inf))

      val result = repo.mostUsedWithCurrency("USD", 10)
      val id = Await.result(result, Duration.Inf);
      id.length must be equalTo (0)
    }


  }
}
