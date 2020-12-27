package kolyadun

import kolyadun.config.ScenariosConfig
import kolyadun.model.SamplesConfig
import kolyadun.service.ScenariosCollector
import kolyadun.service.ScenariosCollector.ScenariosCollector
import org.slf4j.LoggerFactory
import sttp.client.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.{App, ExitCode, Has, Layer, UIO, URIO, ZEnv, ZIO, ZLayer}
import zio.console.Console
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
      result <- service.collect
      _ <- UIO(println(result))
    } yield ()

    val layer: Layer[Throwable, ScenariosCollector] = AsyncHttpClientZioBackend
      .layer() >>>
      ScenariosCollector.live

    program.provideLayer(layer).exitCode
  }
}
