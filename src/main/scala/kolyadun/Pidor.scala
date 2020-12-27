package kolyadun

import sttp.client.basicRequest
import sttp.client._
import cats.syntax.either._
import io.circe._, io.circe.parser._

object Pidor {
  def main(args: Array[String]): Unit = {
    val req = basicRequest.get(uri"http://localhost:5050/test_spec")

    val string =
      "{\"body\":{\"params\":{\"1\":\"pidor\"},\"raw\":\"query { cities(b: $1) { iata }}\"},\"method\":\"post\",\"notificationRules\":{\"notifyOnFailure\":true,\"notifyOnSuccess\":false},\"samples\":{\"maxCombinations\":10,\"minCombinations\":1},\"schedule\":{\"recurring\":\"10 min\"},\"timingBoundaries\":{\"mustSucceedWithin\":5000,\"shouldSucceedWithin\":2000}}"

    parse(string)
    val b = HttpURLConnectionBackend().send(req).body

    println(b)
  }
}
