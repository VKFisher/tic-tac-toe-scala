package simple_scala

import simple_scala.GreetingApp
import zhttp.service.Server
import zio._

object MainApp extends ZIOAppDefault {
  def run =
    Server.start(
      port = 8080,
      http = GreetingApp()
    )
}
