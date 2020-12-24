package kolyadun

import org.slf4j.LoggerFactory
import zio.{App, ExitCode, Has, UIO, URIO, ZEnv, ZIO, ZLayer}
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
      str <- ZIO.access[Has[String]](_.get)
      _ <- UIO(println(str))
    } yield ()

    val layer = ZLayer.succeed("hello world") ++ ZLayer.requires[Console]

    program.provideLayer(layer).exitCode
  }
}
