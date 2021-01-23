package kolyadun.model

import java.util.UUID

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import zio.{Ref, UIO}

object Suite {
  object State {
    implicit lazy val jsonEncoder: Encoder[State] = deriveEncoder

    def initial(id: UUID, tasks: List[Task]): State =
      State(id, List(), List(), tasks)
  }
  final case class State(suiteId: UUID,
                         errors: List[String],
                         warnings: List[String],
                         tasksLeft: List[Task])
}
final case class Suite(id: UUID, tasks: List[Task])

final case class SuitesStates(value: Ref[Map[UUID, Suite.State]])
object SuitesStates {
  def fromSuites(suites: List[Suite]): UIO[SuitesStates] = {
    for {
      ref <- Ref.make(
        suites.foldLeft(Map[UUID, Suite.State]())(
          (acc, suite) =>
            acc + (suite.id -> Suite.State.initial(suite.id, suite.tasks))
        )
      )
    } yield SuitesStates(ref)
  }
}
