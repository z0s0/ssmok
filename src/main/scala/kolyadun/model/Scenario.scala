package kolyadun.model
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

object Scenario {
  implicit lazy val jsonDecoder: Decoder[Scenario] = deriveDecoder
  implicit lazy val bodyDecoder: Decoder[Body] = deriveDecoder
  implicit lazy val authDecoder: Decoder[Auth] = deriveDecoder
  implicit lazy val samplesConfDecoder: Decoder[SamplesConfig] = deriveDecoder
  implicit lazy val scheduleDecoder: Decoder[Schedule] = deriveDecoder
  implicit lazy val notificationConfDecoder: Decoder[NotificationConfig] =
    deriveDecoder
  implicit lazy val timingBoundariesDecoder: Decoder[TimingBoundaries] =
    deriveDecoder
}

final case class Scenario(host: String,
                          path: String,
                          method: Option[String],
                          body: Option[Body],
                          assertStatusCode: Option[Int] = Some(200),
                          samples: Option[SamplesConfig],
                          timingBoundaries: Option[TimingBoundaries],
                          notificationRules: Option[NotificationConfig],
                          schedule: Option[Schedule],
                          auth: Option[Auth])

sealed trait HTTPMethod

object HTTPMethod {
  final case object Post extends HTTPMethod
  final case object Get extends HTTPMethod

  implicit val jsonDecoder: Decoder[HTTPMethod] = deriveDecoder
}

final case class Body(raw: String, params: Map[String, String])
final case class SamplesConfig(minCombinations: Int, maxCombinations: Int)
final case class TimingBoundaries(mustSucceedWithin: Int,
                                  shouldSucceedWithin: Int)
final case class NotificationConfig(notifyOnFailure: Boolean,
                                    notifyOnSuccess: Boolean)
final case class Schedule(recurring: Int)
final case class Auth(method: String, credentials: Map[String, String])
