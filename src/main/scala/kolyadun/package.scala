import sttp.client.SttpBackend
import sttp.client.asynchttpclient.WebSocketHandler
import zio.Task

package object kolyadun {
  type SttpClientService = SttpBackend[Task, Nothing, WebSocketHandler]
}
