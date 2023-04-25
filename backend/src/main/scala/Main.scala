package tictactoe

import zhttp.http.Middleware.cors
import zhttp.http._
import zhttp.http.middleware.Cors.CorsConfig
import zhttp.service.Server
import zio._
import cats.syntax.all._
import cats.implicits._
import tictactoe.infra.http.TicTacToeHttpApp

object MainApp extends ZIOAppDefault {

  val config: CorsConfig =
    CorsConfig(
      anyOrigin = true,
      allowedMethods =
        Some(Set(Method.PUT, Method.GET, Method.POST, Method.DELETE))
    )

  def run =
    ZIO
      .service[TicTacToeHttpApp]
      .flatMap(app =>
        Server.start(
          port = 8080,
          http = app()
            .tapErrorZIO(e => ZIO.logError(e.toString()))
            .catchAll(Http.succeed) @@ cors(config)
        )
      )
      .provide(TicTacToeHttpApp.default)
}
