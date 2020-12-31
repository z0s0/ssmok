package kolyadun.service

import kolyadun.SttpClientService
import kolyadun.config.ScenariosConfig.ScenariosConfig
import kolyadun.model.{Scenario, ServiceDestination}
import sttp.client.asynchttpclient.zio._
import sttp.client._
import sttp.client.circe._
import zio.{Has, Task, URLayer, ZLayer}

object ScenariosCollector {
  type ScenariosCollector = Has[Service]

  trait Service {
    def collect: Task[List[Scenario]]
  }

  val live: URLayer[SttpClient with ScenariosConfig, ScenariosCollector] =
    ZLayer
      .fromServices[SttpClientService, List[ServiceDestination], Service] {
        (client, conf) =>
          new Service {
            override def collect: Task[List[Scenario]] = {
              val list: List[Task[List[Scenario]]] = conf.map(sD => {
                val req =
                  basicRequest
                    .get(uri"${sD.host}")
                    .response(asJson[List[Scenario]])

                client.send(req).map(_.body).absolve
              })

              Task.collectAll(list).map(_.flatten)
            }
          }

      }
}
