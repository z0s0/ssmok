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
        case Scenario(
            host,
            path,
            tag,
            Some(method),
            body,
            assertStatusCodeOpt,
            _,
            timingBoundaries,
            _,
            schedule,
            _
            ) =>
          Suite(
            suiteId,
            List(
              Task(
                id = UUID.randomUUID(),
                host = host,
                path = path,
                tag = tag,
                method = method,
                body = Some(""),
                suiteId = suiteId,
                assertStatusCode = assertStatusCodeOpt.getOrElse(200),
                repeatEvery = schedule.flatMap(v => Some(v.recurring)),
                shouldSucceedWithin =
                  timingBoundaries.flatMap(v => v.shouldSucceedWithin),
                mustSucceedWithin =
                  timingBoundaries.flatMap(v => v.mustSucceedWithin)
              )
            )
          )
      }
    })

}
