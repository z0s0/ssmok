package kolyadun.model

sealed trait TaskType

object TaskType {
  final case object GraphQL extends TaskType
  final case object Classic extends TaskType
}

case class Task(taskType: TaskType)
