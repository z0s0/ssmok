package kolyadun

import kolyadun.api.{HealthCheck, SuitesRoutes}
import kolyadun.api.SuitesRoutes.SuitesRoutes
import kolyadun.config.ScenariosConfig
import kolyadun.model.{SuitesStates, Task}
import kolyadun.service.{ScenariosCollector, SuiteBuilder, Visitor}
import kolyadun.service.ScenariosCollector.ScenariosCollector
import kolyadun.service.SuiteBuilder.SuiteBuilder
import kolyadun.service.Visitor.Visitor
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{App, ExitCode, Layer, Queue, URIO, ZEnv, ZIO}
import zio.internal.Platform
import zio.interop.catz.implicits.ioTimer
import zio.interop.catz._
import org.http4s.implicits._
import cats.implicits._

object Main extends App {
  private val log = LoggerFactory.getLogger("RuntimeReporter")
  override val platform: Platform = Platform.default.withReportFailure {
    cause =>
      log.error(cause.prettyPrint)
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val program = for {
      service <- ZIO.access[ScenariosCollector](_.get)
      suiteBuilder <- ZIO.access[SuiteBuilder](_.get)
      scenarios <- service.collect
      visitor <- ZIO.access[Visitor](_.get)
      q <- Queue.bounded[Task](1000)
      suites <- ZIO.succeed(scenarios.map(suiteBuilder.build))
      suitesStates <- SuitesStates.fromSuites(suites)
      routes <- ZIO.access[SuitesRoutes](
        _.get.route(suitesStates).combineK((new HealthCheck).route)
      )
      tasks <- ZIO.succeed(suites.flatMap(_.tasks))
      _ <- q.offerAll(tasks)
      _ <- visitor.perform(suitesStates, q)
      _ <- startHttp(routes).fork
      _ <- ZIO.never
    } yield ()

    val layer: Layer[
      Throwable,
      ScenariosCollector with SuiteBuilder with Visitor with SuitesRoutes
    ] = (AsyncHttpClientZioBackend
      .layer() ++ ScenariosConfig.live) >>>
      (ScenariosCollector.live ++ SuiteBuilder.live ++ Visitor.live ++ SuitesRoutes.live)

    program.provideCustomLayer(layer).exitCode
  }

  private def startHttp[R](
    routes: HttpRoutes[zio.Task]
  ): ZIO[R, Throwable, Unit] = {
    ZIO.runtime[R].flatMap { implicit rt =>
      val httpApp = Router("api" -> routes).orNotFound

      BlazeServerBuilder[zio.Task]
        .withHttpApp(httpApp)
        .bindHttp(5100, "127.0.0.1")
        .serve
        .compile
        .drain
    }
  }
}
