package kolyadun.config

import kolyadun.model.ServiceDestination
import zio.{Has, ULayer, ZLayer}

object ScenariosConfig {
  type ScenariosConfig = Has[List[ServiceDestination]]

  val live: ULayer[ScenariosConfig] = ZLayer.succeed(
    List(
      ServiceDestination("http://localhost:5050/test_spec"),
      ServiceDestination("http://localhost:5050/test_spec")
    )
  )
}
