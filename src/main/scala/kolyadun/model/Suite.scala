package kolyadun.model

import java.util.UUID

object Suite {
  object State {
    def initial(id: UUID, tasks: List[Task]): State = State(id, List(), tasks)
  }
  case class State(suiteId: UUID, errors: List[String], tasksLeft: List[Task])
}
final case class Suite(id: UUID, tasks: List[Task])
