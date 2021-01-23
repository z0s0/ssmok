package kolyadun.model

import java.util.UUID

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

object Task {
  implicit lazy val jsonEncoder: Encoder[Task] = deriveEncoder
}

final case class Task(id: UUID,
                      host: String,
                      path: String,
                      method: HTTPMethod,
                      body: Option[String],
                      assertStatusCode: Int = 200,
                      suiteId: UUID,
                      mustSucceedWithin: Option[Int],
                      shouldSucceedWithin: Option[Int],
                      repeatEvery: Option[Int] = None)
