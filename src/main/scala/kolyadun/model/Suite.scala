package kolyadun.model

import java.util.UUID

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

object Suite {
  object State {
    implicit lazy val jsonEncoder: Encoder[State] = deriveEncoder

    def initial(id: UUID, tasks: List[Task]): State = State(id, List(), tasks)
  }
  case class State(suiteId: UUID, errors: List[String], tasksLeft: List[Task])
}
final case class Suite(id: UUID, tasks: List[Task])
