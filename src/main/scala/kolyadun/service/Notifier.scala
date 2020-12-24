package kolyadun.service

import zio.{Has, UIO}
import zio.macros.accessible

@accessible
object Notifier {
  type Notifier = Has[Service]

  trait Service {
    def notify(msg: String): UIO[Unit]
  }

  val live = ???
}
