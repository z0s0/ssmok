package kolyadun.model

import java.util.UUID

final case class Task(id: UUID,
                      host: String,
                      path: String,
                      method: HTTPMethod,
                      body: Option[String],
                      assertStatusCode: Int = 200,
                      suiteId: UUID,
                      repeatEvery: Option[Int] = None)
