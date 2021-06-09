val slickVersion: String = "3.3.2"
val sqliteVersion: String = "3.34.0"
val h2Version: String = "1.4.199"
val liquibaseVersion: String = "3.4.2"
val swaggerUIVersion: String = "3.25.3"
val webjarsVersion: String = "2.8.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .settings(
    name := """galvanize-products""",
    version := "2.7.x",
    scalaVersion := "2.12.2",
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.slick" %% "slick" % slickVersion,
      "org.xerial" % "sqlite-jdbc" % sqliteVersion,
      "com.h2database" % "h2" % h2Version,
      "org.liquibase" % "liquibase-core" % liquibaseVersion,
      "org.webjars" %% "webjars-play" % webjarsVersion,
      "org.webjars" % "swagger-ui" %  swaggerUIVersion,
      ws,
      specs2 % Test,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    ),
  )

swaggerDomainNameSpaces := Seq("dtos")
