package kolyadun.model

sealed trait Scenario

object Scenario {
  final case class GraphQLScenario(host: String) extends Scenario
  final case class ClassicScenario(host: String) extends Scenario
}
