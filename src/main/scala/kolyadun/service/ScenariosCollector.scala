package kolyadun.service

import kolyadun.SttpClientService
import kolyadun.model.{Scenario, ServiceDestination}
import sttp.client.asynchttpclient.zio._
import sttp.client._
import sttp.client.circe._
import zio.clock.Clock
import zio.{Has, Task, ZIO, Schedule, ZLayer}
import zio.duration._

object ScenariosCollector {
  type ScenariosCollector = Has[Service]

  trait Service {
    def collect: ZIO[Clock, Throwable, List[Scenario]]
  }

  val live: ZLayer[Has[SttpClientService] with Has[List[ServiceDestination]],
                   Nothing,
                   Has[Service]] =
    ZLayer
      .fromServices[SttpClientService, List[ServiceDestination], Service] {
        (client, conf) =>
          new Service {
            override def collect: ZIO[Clock, Throwable, List[Scenario]] = {
              val schedule = Schedule.exponential(1.millis) && Schedule.recurs(
                100
              )

              val list: List[Task[List[Scenario]]] = conf.map(sD => {
                val req =
                  basicRequest
                    .get(uri"${sD.host}")
                    .response(asJson[List[Scenario]])

                client.send(req).map(_.body).absolve
              })

              Task.collectAll(list).retry(schedule).map(_.flatten)
            }
          }

      }
}
