package kolyadun

import java.util.UUID

import kolyadun.config.ScenariosConfig
import kolyadun.model.{Suite, Task}
import kolyadun.service.{ScenariosCollector, SuiteBuilder}
import kolyadun.service.ScenariosCollector.ScenariosCollector
import kolyadun.service.SuiteBuilder.SuiteBuilder
import org.slf4j.LoggerFactory
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{App, ExitCode, Layer, Queue, Ref, UIO, URIO, ZEnv, ZIO}
import zio.internal.Platform

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
      q <- Queue.bounded[Task](1000)
      suites <- ZIO.succeed(scenarios.map(suiteBuilder.build))
      suitesStates <- Ref.make(
        suites.foldLeft(Map[UUID, Suite.State]())(
          (acc, suite) =>
            acc + (suite.id -> Suite.State.initial(suite.id, suite.tasks))
        )
      )
      _ <- UIO(suitesStates)
      tasks <- ZIO.succeed(suites.flatMap(_.tasks))
      _ <- UIO(println(suites))
      _ <- q.offerAll(tasks)
    } yield ()

    val layer: Layer[Throwable, ScenariosCollector with SuiteBuilder] = (AsyncHttpClientZioBackend
      .layer() ++ ScenariosConfig.live) >>>
      (ScenariosCollector.live ++ SuiteBuilder.live)

    program.provideLayer(layer).exitCode
  }
}
