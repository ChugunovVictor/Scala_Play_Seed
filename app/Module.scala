import com.google.inject.AbstractModule
import javax.inject._
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import play.api.Environment
import play.api.inject.ApplicationLifecycle
import utils.App

import scala.concurrent.ExecutionContext

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[ApplicationStart]).asEagerSingleton()
  }
}

/**
 * Manual start of liquibase integration
 * on every application init
 */
@Singleton
class ApplicationStart @Inject()(implicit ec: ExecutionContext,
                                 lifecycle: ApplicationLifecycle,
                                 environment: Environment) {
  val jdbc: JdbcConnection = new JdbcConnection(App.db.source.createConnection())
  val lb: Liquibase = new Liquibase(
    "liquibase/changelog.xml",
    new ClassLoaderResourceAccessor(),
    jdbc)
  lb.update("")
}

