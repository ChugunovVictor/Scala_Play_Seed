package utils

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Future

object App{
  var config = ConfigFactory.load("application.conf")
  var db: Database = Database.forConfig("jdbc", config)

  def checkDatabaseConnection(): Future[Int] = db.run {
    sql"""select 1""".as[Int].head
  }
}
