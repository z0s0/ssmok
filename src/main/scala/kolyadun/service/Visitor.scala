package kolyadun.service

import cats.data.Validated
import kolyadun.SttpClientService
import kolyadun.model.{HTTPMethod, Suite, SuitesStates, Task}
import zio.{Has, Queue, ZIO, ZLayer}
import sttp.client.asynchttpclient.zio._
import sttp.client._
import sttp.client.circe._
import zio.clock.Clock
import zio.duration._

object Visitor {
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
        (responseTime, response) <- client.send(request).timed
        _ <- states.value.update(
          map =>
            map.get(suiteId) match {
              case Some(state) =>
                val newState =
                  newStateFromResponseAndTask(
                    response,
                    state,
                    task,
                    responseTime.toMillis
                  )
                map + (suiteId -> newState)
              case None => map
          }
        )

      } yield ()
    }

    private def newStateFromResponseAndTask(response: Response[_],
                                            state: Suite.State,
                                            task: Task,
                                            responseTime: Long): Suite.State = {
      val responseCode = response.code.code
      val statusCodeValidation = Validated.cond(
        responseCode == task.assertStatusCode,
        "ok",
        List(s"Status code ${responseCode}. ${task.assertStatusCode} expected")
      )
      val responseTimeValidation = Validated.cond(
        task.mustSucceedWithin.fold(true)(_ >= responseTime),
        "ok",
        List(
          s"task ${task.tag} expected to succeed within ${task.mustSucceedWithin.get}ms but response time was ${responseTime}ms"
        )
      )
      val errorValidation = statusCodeValidation.combine(responseTimeValidation)
      val tasksLeft = state.tasksLeft.filter(_.id != task.id)
      val newWarnings =
        if (task.shouldSucceedWithin.fold(true)(_ >= responseTime))
          state.warnings
        else
          s"task ${task.tag} expected to succeed within ${task.shouldSucceedWithin.get}ms but response time was ${responseTime}ms" :: state.warnings

      errorValidation match {
        case Validated.Valid(_) =>
          state.copy(tasksLeft = tasksLeft, warnings = newWarnings)
        case Validated.Invalid(errs) =>
          state.copy(
            tasksLeft = tasksLeft,
            errors = errs.mkString(", ") :: state.errors,
            warnings = newWarnings
          )
      }
    }
  }

  val live: ZLayer[Has[SttpClientService], Throwable, Has[Service]] =
    ZLayer.fromService(new Live(_))
}
