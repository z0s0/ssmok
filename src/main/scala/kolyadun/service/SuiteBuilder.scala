package kolyadun.service

import java.util.UUID

import kolyadun.model.{Scenario, Suite, Task}
import zio.{Has, ULayer, ZLayer}

object SuiteBuilder {

  type SuiteBuilder = Has[Service]

  trait Service {
    def build(scenario: Scenario): Suite
  }

  val live: ULayer[SuiteBuilder] =
    ZLayer.succeed((scenario: Scenario) => {
      val suiteId = UUID.randomUUID()

      scenario match {
        case Scenario(host, path, _, _, _, _, _, _, _, _) =>
          Suite(
            suiteId,
            List(
              Task(
                id = UUID.randomUUID(),
                host = host,
                path = path,
                body = None,
                suiteId = suiteId
              )
            )
          )
      }
    })

}
