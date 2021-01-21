package kolyadun.service

import java.util.UUID

import kolyadun.SttpClientService
import kolyadun.model.{Suite, Task}
import zio.{Has, Queue, Ref, UIO, ZIO, ZLayer}
import sttp.client.asynchttpclient.zio._
import sttp.client._
import sttp.client.circe._

object Visitor {
  type SuitesStates = Ref[Map[UUID, Suite.State]]
  type Visitor = Has[Service]

  trait Service {
    def perform(suitesStates: SuitesStates,
                queue: Queue[Task]): ZIO[Any, Serializable, Nothing]
  }

  class Live(client: SttpClientService) extends Service {
    override def perform(
      suitesStates: SuitesStates,
      queue: Queue[Task]
    ): ZIO[Any, Serializable, Nothing] = {
      val worker =
        queue.take.flatMap(performSingleTask(_, suitesStates)).forever

      ZIO.forkAll(List.fill(20)(worker)).flatMap(_.join) *> ZIO.never
    }

    private def performSingleTask(task: Task, states: SuitesStates) = {
      val suiteId = task.suiteId
      val request =
        basicRequest.get(uri"${task.host}${task.path}")

      client.send(request).map(_.body).absolve

      states.update(
        map =>
          map.get(suiteId) match {
            case Some(state) =>
              val newState =
                state.copy(tasksLeft = state.tasksLeft.filter(_.id != task.id))
              map + (suiteId -> newState)
            case None => map
        }
      )
    }
  }
  val live: ZLayer[Has[SttpClientService], Throwable, Has[Service]] =
    ZLayer.fromService(new Live(_))
}
