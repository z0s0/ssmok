package kolyadun

import kolyadun.api.SuitesRoutes.SuitesRoutes
import kolyadun.config.ApiConfig
import kolyadun.model.{SuitesStates, Task}
import kolyadun.service.ScenariosCollector.ScenariosCollector
import kolyadun.service.SuiteBuilder.SuiteBuilder
import kolyadun.service.Visitor.Visitor
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory
import zio.{App, ExitCode, Has, Queue, URIO, ZEnv, ZIO}
import zio.internal.Platform
import zio.interop.catz.implicits.ioTimer
import zio.interop.catz._
import org.http4s.implicits._

object Main extends App {
  private val log = LoggerFactory.getLogger("RuntimeReporter")
  override val platform: Platform = Platform.default.withReportFailure {
    cause =>
      log.error(cause.prettyPrint)
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val program = for {
      apiConf <- ZIO.access[Has[ApiConfig]](_.get)
      service <- ZIO.access[ScenariosCollector](_.get)
      suiteBuilder <- ZIO.access[SuiteBuilder](_.get)
      scenarios <- service.collect
      visitor <- ZIO.access[Visitor](_.get)
      q <- Queue.bounded[Task](1000)
      suites <- ZIO.succeed(
        scenarios.values.flatten.toList.map(suiteBuilder.build)
      )
      suitesStates <- SuitesStates.fromSuites(suites)
      routes <- ZIO.access[SuitesRoutes](_.get.route(suitesStates))
      tasks <- ZIO.succeed(suites.flatMap(_.tasks))
      _ <- q.offerAll(tasks)
      _ <- visitor.perform(suitesStates, q).fork
      _ <- startHttp(routes, apiConf)
      _ <- ZIO.never
    } yield ()

    program.provideCustomLayer(DI.live).exitCode
  }

  private def startHttp[R](routes: HttpRoutes[zio.Task],
                           apiConfig: ApiConfig): ZIO[R, Throwable, Unit] = {
    ZIO.runtime[R].flatMap { implicit rt =>
      val httpApp = Router("api" -> routes).orNotFound

      BlazeServerBuilder[zio.Task]
        .withHttpApp(httpApp)
        .bindHttp(apiConfig.port, "127.0.0.1")
        .serve
        .compile
        .drain
    }
  }
}
