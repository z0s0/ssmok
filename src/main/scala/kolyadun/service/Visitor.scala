package kolyadun.service

import java.util.UUID

import kolyadun.SttpClientService
import kolyadun.model.{HTTPMethod, Suite, Task}
import zio.{Has, Queue, Ref, UIO, ZIO, ZLayer}
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
        v <- states.get
        response <- client.send(request)
        _ <- states.update(
          map =>
            map.get(suiteId) match {
              case Some(state) =>
                val newState =
                  newStateFromResponseAndTask(response, state, task)
                map + (suiteId -> newState)
              case None => map
          }
        )

      } yield ()
    }

    private def newStateFromResponseAndTask(response: Response[_],
                                            state: Suite.State,
                                            task: Task): Suite.State = {
      val tasksLeft = state.tasksLeft.filter(_.id != task.id)
      val responseCode = response.code.code

      if (responseCode == task.assertStatusCode) {
        state.copy(tasksLeft = tasksLeft)
      } else {
        val err =
          s"Status code ${responseCode}. ${task.assertStatusCode} expected"
        state.copy(tasksLeft = tasksLeft, errors = err :: state.errors)
      }
    }
  }

  val live: ZLayer[Has[SttpClientService], Throwable, Has[Service]] =
    ZLayer.fromService(new Live(_))
}
