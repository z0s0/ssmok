package kolyadun.api

import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import kolyadun.model.SuitesStates
import zio.{Has, Ref, Task, ULayer, ZLayer}

object SuitesRoutes {
  type SuitesRoutes = Has[Service]

  trait Service {
    def route(suitesStates: SuitesStates): HttpRoutes[Task]
  }

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[Task, A] = jsonOf
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[Task, A] =
    jsonEncoderOf

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
