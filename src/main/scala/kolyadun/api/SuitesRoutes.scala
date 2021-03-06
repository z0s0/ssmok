package kolyadun.api

import zio.interop.catz._
import zio.{Has, Task, ULayer, ZLayer}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import kolyadun.model.SuitesStates

object SuitesRoutes {
  type SuitesRoutes = Has[Service]

  trait Service {
    def route(suitesStates: SuitesStates): HttpRoutes[Task]
  }

  val live: ULayer[SuitesRoutes] = ZLayer.succeed(new SuitesRouter())

  final class SuitesRouter extends SuitesRoutes.Service with Http4sDsl[Task] {
    override def route(suitesStates: SuitesStates): HttpRoutes[Task] =
      HttpRoutes.of[Task] {
        case GET -> Root / "suites" => {

          Ok(suitesStates.value.get)
        }
      }
  }
}
