package kolyadun.model

import java.util.UUID

final case class Task(host: String,
                      path: String,
//                      method: HTTPMethod,
                      body: Option[String],
                      assertStatusCode: Int = 200,
                      suiteId: UUID)
