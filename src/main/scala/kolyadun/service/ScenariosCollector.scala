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
  type SourceResponse = Map[String, List[Scenario]]
  type ScenariosByApp = Map[String, List[Scenario]]

  type ScenariosCollector = Has[Service]

  trait Service {
    def collect: ZIO[Clock, Throwable, ScenariosByApp]
  }

  val live: ZLayer[Has[SttpClientService] with Has[List[ServiceDestination]],
                   Nothing,
                   Has[Service]] =
    ZLayer
      .fromServices[SttpClientService, List[ServiceDestination], Service] {
        (client, conf) =>
          new Service {
            override def collect: ZIO[Clock, Throwable, ScenariosByApp] = {
              val schedule = Schedule.exponential(1.millis) && Schedule.recurs(
                100
              )

              val list: List[Task[SourceResponse]] = conf.map(sD => {
                val req =
                  basicRequest
                    .get(uri"${sD.host}")
                    .response(asJson[SourceResponse])

                client.send(req).map(_.body).absolve
              })

              Task.collectAll(list).map { allSourcesResponses =>
                allSourcesResponses
                  .foldLeft(Map[String, List[Scenario]]()) {
                    (acc, oneSourceResp) =>
                      val sourceApps = oneSourceResp.keys.toList

                      sourceApps.foldLeft(acc) { (acc, appKey) =>
                        acc.get(appKey) match {
                          case Some(alreadyStoredScenarios) =>
                            acc + (appKey -> (oneSourceResp(appKey) ++ alreadyStoredScenarios))
                          case None =>
                            acc + (appKey -> oneSourceResp(appKey))
                        }
                      }
                  }
              }
            }
          }

      }
}
