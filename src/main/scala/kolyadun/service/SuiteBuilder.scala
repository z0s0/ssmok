package kolyadun.service

import java.util.UUID

import kolyadun.model.{Scenario, Suite, Task}
import kolyadun.service.TaskQueue.TaskQueue
import zio.{Has, URLayer, ZLayer}

object SuiteBuilder {

  type SuiteBuilder = Has[Service]

  trait Service {
    def build(scenario: Scenario): Suite
  }

  val live: URLayer[TaskQueue, SuiteBuilder] =
    ZLayer.fromService { queue => (scenario: Scenario) =>
      {
        val suiteId = UUID.randomUUID()
        val statusCode = scenario.assertStatusCode.getOrElse(200)
        val body = ""
        val tasks = List(
          Task(
            host = scenario.host,
            path = scenario.path,
            suiteId = suiteId,
            assertStatusCode = statusCode,
            body = Some(body)
          )
        )

        queue.offerAll(tasks)

        Suite(suiteId, tasks)
      }
    }

}
