package ssmok

import zio.{App, ExitCode, Has, UIO, URIO, ZEnv, ZIO, ZLayer}

object Main extends App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val program = for {
      str <- ZIO.access[Has[String]](_.get)
      _ <- UIO(println(str))
    } yield ()

    val layer = ZLayer.succeed("hello world")
    program.provideLayer(layer).exitCode
  }
}
