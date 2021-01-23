package kolyadun.api

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import zio.{Has, Task, ULayer, ZLayer}

object SuitesRoutes {
  type SuitesRoutes = Has[Service]

  trait Service {
    def route: HttpRoutes[Task]
  }

  val live: ULayer[SuitesRoutes] = ZLayer.succeed(new SuitesRouter())

  final class SuitesRouter extends SuitesRoutes.Service with Http4sDsl[Task] {
    override def route: HttpRoutes[Task] = HttpRoutes.of[Task] {
      case GET -> Root / "suites" => Ok("")
    }
  }
}
