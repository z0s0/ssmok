package kolyadun

import kolyadun.api.SuitesRoutes
import kolyadun.service.{ScenariosCollector, SuiteBuilder, Visitor}
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import kolyadun.config.{Layer => ConfigLayer}

object DI {
  val live =
    (AsyncHttpClientZioBackend
      .layer() ++ ConfigLayer.live) >>>
      (ScenariosCollector.live ++ SuiteBuilder.live ++ Visitor.live ++ SuitesRoutes.live ++ ConfigLayer.live)
}
