package simple_scala

import simple_scala.GreetingApp
import zhttp.http.Middleware.cors
import zhttp.http._
import zhttp.http.middleware.Cors.CorsConfig
import zhttp.service.Server
import zio._
import cats.syntax.all._
import cats.implicits._


object MainApp extends ZIOAppDefault {

  val config: CorsConfig =
    CorsConfig(allowedOrigins = _ === "dev", allowedMethods = Some(Set(Method.PUT, Method.DELETE)))

  def run =
    Server.start(
      port = 8080,
      http = TicTacToeApp() @@ cors(config)
    )
}
