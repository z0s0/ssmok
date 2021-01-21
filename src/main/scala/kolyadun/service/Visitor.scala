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
                queue: Queue[Task]): ZIO[Any, Serializable, Unit]
  }

  class Live(client: SttpClientService) extends Service {
    override def perform(suitesStates: SuitesStates,
                         queue: Queue[Task]): ZIO[Any, Serializable, Unit] =
      for {
        _ <- ZIO.foreach(0 to 10) { _ =>
          queue.take
            .flatMap(performSingleTask(_, suitesStates, queue))
            .uninterruptible
            .forever
            .fork
        }
      } yield ()

    private def performSingleTask(task: Task,
                                  states: SuitesStates,
                                  queue: Queue[Task]) = {
      val suiteId = task.suiteId
      val url = task.host + task.path

      val request =
        basicRequest.get(uri"$url")

      for {
        _ <- client.send(request).map(_.body)
        _ <- states.update(
          map =>
            map.get(suiteId) match {
              case Some(state) =>
                val newState =
                  state.copy(
                    tasksLeft = state.tasksLeft.filter(_.id != task.id)
                  )
                map + (suiteId -> newState)
              case None => map
          }
        )

        _ <- queue.offer(task.copy(id = UUID.randomUUID()))
      } yield ()
    }
  }
  val live: ZLayer[Has[SttpClientService], Throwable, Has[Service]] =
    ZLayer.fromService(new Live(_))
}
