package kolyadun.model
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax.EncoderOps

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
                          method: Option[HTTPMethod] = Some(HTTPMethod.Get),
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

  implicit lazy val httpMethodEncoder: Encoder[HTTPMethod] = Encoder.instance {
    case HTTPMethod.Post => "post".asJson
    case HTTPMethod.Get  => "get".asJson
  }

  implicit lazy val httpMethodDecoder: Decoder[HTTPMethod] =
    Decoder[String].emap {
      case "get"      => Right(HTTPMethod.Get)
      case "post"     => Right(HTTPMethod.Post)
      case unknownVal => Left(s"invalid $unknownVal")
    }
}

final case class Body(raw: String, params: Map[String, String])
final case class SamplesConfig(minCombinations: Int, maxCombinations: Int)
final case class TimingBoundaries(mustSucceedWithin: Option[Int],
                                  shouldSucceedWithin: Option[Int])
final case class NotificationConfig(notifyOnFailure: Boolean,
                                    notifyOnSuccess: Boolean)
final case class Schedule(recurring: Int)
final case class Auth(method: String, credentials: Map[String, String])
