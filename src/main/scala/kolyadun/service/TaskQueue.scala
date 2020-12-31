package kolyadun.service

import kolyadun.model.Task
import zio.{Has, UIO, ULayer, ZLayer, Queue => ZQueue}

object TaskQueue {

  type TaskQueue = Has[ZQueue[Task]]

  val live: ULayer[Has[UIO[ZQueue[Task]]]] =
    ZLayer.succeed(ZQueue.bounded[Task](10000))
}
