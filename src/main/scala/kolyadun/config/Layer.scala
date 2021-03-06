package kolyadun.config

import kolyadun.model.ServiceDestination
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{Has, Layer, Task, ZLayer}

final case class Config(apiConfig: ApiConfig,
                        serviceDestinations: List[ServiceDestination])
final case class ApiConfig(port: Int)

object Layer {
  type Configs = Has[ApiConfig] with Has[List[ServiceDestination]]

  val live: Layer[Throwable, Configs] =
    ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[Config])
        .map(conf => Has(conf.apiConfig) ++ Has(conf.serviceDestinations))
    )
}
