package kolyadun.model
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax.EncoderOps

object Scenario {
  implicit lazy val jsonDecoder: Decoder[Scenario] = deriveDecoder
  implicit lazy val bodyDecoder: Decoder[Body] = deriveDecoder
  implicit lazy val samplesConfDecoder: Decoder[SamplesConfig] = deriveDecoder
  implicit lazy val scheduleDecoder: Decoder[Schedule] = deriveDecoder
  implicit lazy val notificationConfDecoder: Decoder[NotificationConfig] =
    deriveDecoder
  implicit lazy val timingBoundariesDecoder: Decoder[TimingBoundaries] =
    deriveDecoder
}

/*
%{
        recurringInMinutes: 10,
        method: :post,
        body: %{
          raw: "query { cities(b: $1) { iata }}",
          params: %{
            1 => "pidor"
          }
        },
        samples: %{
          minCombinations: 1,
          maxCombinations: 10
        },
        timingBoundaries: %{
          mustSucceedWithin: 5000,
          shouldSucceedWithin: 2000,
        },
        notificationRules: %{
          notifyOnFailure: true,
          notifyOnSuccess: false
        }
      }
 */
final case class Scenario(host: String,
                          path: String,
//                          method: HTTPMethod,
                          body: Option[Body],
                          assertStatusCode: Option[Int] = Some(200),
                          samples: SamplesConfig,
                          timingBoundaries: TimingBoundaries,
                          notificationRules: NotificationConfig,
                          schedule: Schedule)

sealed trait HTTPMethod

object HTTPMethod {
  case object Post extends HTTPMethod
  case object Get extends HTTPMethod

  implicit val jsonDecoder: Decoder[HTTPMethod] = deriveDecoder
}

final case class Body(raw: String, params: Map[String, String])
final case class SamplesConfig(minCombinations: Int, maxCombinations: Int)
final case class TimingBoundaries(mustSucceedWithin: Int,
                                  shouldSucceedWithin: Int)
final case class NotificationConfig(notifyOnFailure: Boolean,
                                    notifyOnSuccess: Boolean)
final case class Schedule(recurring: String)
