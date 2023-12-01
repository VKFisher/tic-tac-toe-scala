package tictactoe

import cats.implicits.*
import cats.syntax.all.*
import tictactoe.infra.GameStateUpdater
import tictactoe.infra.http.TicTacToeHttpApp
import tictactoe.infra.logging.Logging.devLoggingSetup
import tictactoe.infra.repo.InMemoryGameStateRepository
import zhttp.http.*
import zhttp.http.Middleware.cors
import zhttp.http.middleware.Cors.CorsConfig
import zhttp.service.Server
import zio.*

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    devLoggingSetup(LogLevel.Debug)

  def layer: TaskLayer[TicTacToeHttpApp with GameStateUpdater] =
    ZLayer.make[TicTacToeHttpApp with GameStateUpdater](
      InMemoryGameStateRepository.layer,
      GameStateUpdater.layer,
      TicTacToeHttpApp.layer
    )

  val config: CorsConfig =
    CorsConfig(
      anyOrigin = true,
      allowedMethods =
        Some(Set(Method.PUT, Method.GET, Method.POST, Method.DELETE))
    )

  private def main = {
    val runStateUpdater = ZIO
      .service[GameStateUpdater]
      .flatMap(_.run)
    val runServer = ZIO
      .service[TicTacToeHttpApp]
      .flatMap(app =>
        Server.start(
          port = 8080,
          http = app()
            .tapErrorZIO(e => ZIO.logError(e.toString))
            .catchAll(Http.succeed) @@ cors(config)
        )
      )
    ZIO.logInfo("Initializing...") *> runStateUpdater.fork *> runServer
  }
  def run: ZIO[Any, Throwable, Nothing] =
    main.provide(layer)
}
