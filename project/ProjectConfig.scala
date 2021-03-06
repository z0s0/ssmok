import sbt._

object ProjectConfig {
  object versions {
    val zio = "1.0.3"
    val http4s = "0.21.11"
    val interopCats = "2.2.0.1"
    val `zio-config` = "1.0.0-RC27"
    val `zio-logging` = "0.5.2"

    val logback = "1.2.3"

    val slf4j = "1.7.30"
    val sttp = "2.1.4"
    val circe = "0.13.0"
    val pureConfigVersion = "0.14.0"
  }

  val configDeps = List(
    "com.github.pureconfig" %% "pureconfig" % versions.pureConfigVersion
  )

  val zioDeps = Seq(
    "dev.zio" %% "zio" % versions.zio,
    "dev.zio" %% "zio-macros" % versions.zio,
    "dev.zio" %% "zio-interop-cats" % versions.interopCats,
    "dev.zio" %% "zio-config" % versions.`zio-config`,
    "dev.zio" %% "zio-config-magnolia" % versions.`zio-config`,
    "dev.zio" %% "zio-config-typesafe" % versions.`zio-config`,
    "dev.zio" %% "zio-test" % versions.zio % Test,
    "dev.zio" %% "zio-test-sbt" % versions.zio % Test,
    "dev.zio" %% "zio-test-magnolia" % versions.zio % Test,
    "dev.zio" %% "zio-logging" % versions.`zio-logging`,
    "dev.zio" %% "zio-logging-slf4j" % versions.`zio-logging`
  )
  val http4sDeps = Seq(
    "org.http4s" %% "http4s-blaze-server" % versions.http4s,
    "org.http4s" %% "http4s-circe" % versions.http4s,
    "org.http4s" %% "http4s-dsl" % versions.http4s
  )

  val logDependencies = Seq(
    "ch.qos.logback" % "logback-classic" % versions.logback,
    "org.slf4j" % "slf4j-api" % versions.slf4j
  )

  val sttpDeps = Seq(
    "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % versions.sttp,
    "com.softwaremill.sttp.client" %% "circe" % versions.sttp
  )
  val circeDeps = Seq(
    "io.circe" %% "circe-yaml" % "0.12.0",
    "io.circe" %% "circe-generic" % versions.circe,
    "io.circe" %% "circe-generic-extras" % versions.circe
  )
  val deps = zioDeps ++ http4sDeps ++ circeDeps ++ logDependencies ++ sttpDeps ++ configDeps
}
