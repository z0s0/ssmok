package kolyadun.service

import java.util.UUID

import kolyadun.SttpClientService
import kolyadun.model.{HTTPMethod, Suite, Task}
import zio.{Has, Queue, Ref, ZIO, ZLayer}
import sttp.client.asynchttpclient.zio._
import sttp.client._
import sttp.client.circe._
import zio.clock.Clock
import zio.duration._

object Visitor {
  type SuitesStates = Ref[Map[UUID, Suite.State]]
  type Visitor = Has[Service]

  trait Service {
    def perform(suitesStates: SuitesStates,
                queue: Queue[Task]): ZIO[Clock, Serializable, Unit]
  }

  class Live(client: SttpClientService) extends Service {
    override def perform(suitesStates: SuitesStates,
                         queue: Queue[Task]): ZIO[Clock, Serializable, Unit] =
      for {
        _ <- ZIO.foreach_(0 to 10) { _ =>
          queue.take
            .flatMap(task => {
              for {
                effect <- performSingleTask(task, suitesStates)
                _ <- task.repeatEvery match {
                  case Some(r) => queue.offer(task).forkDaemon.delay(r.seconds)
                  case None    => ZIO.unit
                }
              } yield effect
            })
            .uninterruptible
            .forever
            .fork
        }
      } yield ()

    private def performSingleTask(
      task: Task,
      states: SuitesStates
    ): ZIO[Clock, Throwable, Unit] = {
      val suiteId = task.suiteId
      val url = task.host + task.path

      val request = task.method match {
        case HTTPMethod.Post => basicRequest.post(uri"$url").body(task.body)
        case HTTPMethod.Get  => basicRequest.get(uri"$url")
      }

      for {
        _ <- client.send(request)
        _ <- ZIO.effect(client.close())
        _ <- states.update(
          map =>
            map.get(suiteId) match {
              case Some(state) =>
                val newState =
                  state
                    .copy(tasksLeft = state.tasksLeft.filter(_.id != task.id))
                map + (suiteId -> newState)
              case None => map
          }
        )

      } yield ()
    }
  }
  val live: ZLayer[Has[SttpClientService], Throwable, Has[Service]] =
    ZLayer.fromService(new Live(_))
}
