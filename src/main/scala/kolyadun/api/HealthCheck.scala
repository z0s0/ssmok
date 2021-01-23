package kolyadun.api

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import zio.Task
import zio.interop.catz._

final class HealthCheck extends Http4sDsl[Task] {
  def route: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case GET -> Root / "healthz" => Ok("")
  }
}
