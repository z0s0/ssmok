package kolyadun.service

import kolyadun.config.ScenariosConfig.ScenariosConfig
import kolyadun.model.Scenario
import sttp.client.asynchttpclient.zio.SttpClient
import zio.{Has, Task, URLayer, ZIO, ZLayer}

object ScenariosCollector {
  type ScenariosCollector = Has[Service]

  trait Service {
    def collect: Task[List[Scenario]]
  }

  type Deps = Has[SttpClient] with ScenariosConfig
  val live: URLayer[Deps, ScenariosCollector] = ZLayer.fromFunction { ctx =>
    val httpClient = ctx.get[SttpClient]

    ???
  }
}
