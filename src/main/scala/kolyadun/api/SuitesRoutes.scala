package kolyadun.api

import java.util.UUID

import kolyadun.model.Suite
import kolyadun.service.Visitor.SuitesStates
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import zio.{Has, Ref, Task, ULayer, ZLayer}

object SuitesRoutes {
  type SuitesRoutes = Has[Service]

  trait Service {
    def route: HttpRoutes[Task]
  }

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[Task, A] = jsonOf
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[Task, A] =
    jsonEncoderOf

  val live: ULayer[SuitesRoutes] = ZLayer.succeed(new SuitesRouter())

  final class SuitesRouter extends SuitesRoutes.Service with Http4sDsl[Task] {
    override def route: HttpRoutes[Task] = HttpRoutes.of[Task] {
      case GET -> Root / "suites" => {
        val suitesStates = Ref.make(
          Map[UUID, Suite.State](
            UUID.randomUUID() -> Suite.State.initial(UUID.randomUUID(), List())
          )
        )

        for {
          ref <- suitesStates
          v <- Ok(ref.get)

        } yield v

      }
    }
  }
}
