package kolyadun.model

import io.circe.Decoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object Scenario {
  implicit lazy val jsonDecoder: Decoder[Scenario] = deriveDecoder
}

final case class Scenario(host: String,
                          path: String,
                          method: HTTPMethod,
                          body: Option[Body],
                          assertStatusCode: Int = 200,
                          samplesConfig: SamplesConfig,
                          timingBoundaries: TimingBoundaries,
                          notificationConfig: NotificationConfig,
                          schedule: Schedule)

sealed trait HTTPMethod

object HTTPMethod {
  case object POST extends HTTPMethod
  case object GET extends HTTPMethod
}

final case class Body(raw: String, params: Map[String, String])
final case class SamplesConfig(minCombinations: Int, maxCombinations: Int)
final case class TimingBoundaries(mustSucceedWithin: Int,
                                  shouldSucceedWithin: Int)
final case class NotificationConfig(notifyOnFailure: Boolean,
                                    notifyOnSuccess: Boolean)
final case class Schedule(recurring: String)
