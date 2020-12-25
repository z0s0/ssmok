package kolyadun.service

import kolyadun.config.ScenariosConfig.ScenariosConfig
import kolyadun.model._

import sttp.client.asynchttpclient.zio.SttpClient
import sttp.client._
import sttp.client.circe._
import zio.{Has, Task, URLayer, ZIO, ZLayer}

object ScenariosCollector {
  type ScenariosCollector = Has[Service]

  trait Service {
    def collect: Task[List[Scenario]]
  }

  type Deps = SttpClient with ScenariosConfig
  val live: URLayer[Deps, ScenariosCollector] = ZLayer.fromService { http =>
    new Service {
      override def collect: Task[List[Scenario]] = {
        val req = basicRequest
          .get(uri"http://localhost:5050/test_spec")
          .response(asJson[List[Scenario]])

        val res = http.send(req).map(_.body).absolve
        println(res)
        res
      }

    }

  }
}
