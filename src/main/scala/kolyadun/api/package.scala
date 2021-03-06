package kolyadun

import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import zio.Task
import zio.interop.catz._
import io.circe.{Decoder, Encoder}

package object api {
  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[Task, A] = jsonOf
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[Task, A] =
    jsonEncoderOf
}
