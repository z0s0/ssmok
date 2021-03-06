package kolyadun.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{Has, Layer, Task}

final case class Config(apiConfig: ApiConfig)
final case class ApiConfig(port: Int)

object Layer {
  type Configs = Has[ApiConfig]

  val live: Layer[Throwable, Configs] =
    Task
      .effect(ConfigSource.default.loadOrThrow[Config])
      .map(conf => conf.apiConfig)
      .toLayer

}
