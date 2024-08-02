package tictactoe

import zio._
import zio.http._

import tictactoe.infra.GameStateUpdater
import tictactoe.infra.http.TicTacToeHttpApp
import tictactoe.infra.logging.Logging.defaultLoggingSetup
import tictactoe.infra.repo.InMemoryGameStateRepository
import zio.http.Server.Config.ResponseCompressionConfig

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    defaultLoggingSetup

  def layer: TaskLayer[TicTacToeHttpApp & GameStateUpdater & Server] =
    ZLayer.make[TicTacToeHttpApp & GameStateUpdater & Server](
      InMemoryGameStateRepository.layer,
      GameStateUpdater.layer,
      TicTacToeHttpApp.layer,
      ZLayer.succeed(
        Server.Config.default
          .binding(hostname = "localhost", port = 8000)
          .requestDecompression(isStrict = true)
          .responseCompression(
            ResponseCompressionConfig.default // gzip, deflate
          )
      ),
      Server.live
    )

  private def main = {
    val runStateUpdater = ZIO
      .service[GameStateUpdater]
      .flatMap(_.run)
      .zipPar(ZIO.logInfo("Starting GameStateUpdater"))
    val runServer = ZIO
      .service[TicTacToeHttpApp]
      .flatMap(app => Server.serve(app.routes))
      .zipParLeft(ZIO.logInfo("Starting TicTacToeHttpApp"))
    ZIO.logInfo("Initializing tic-tac-toe backend")
      *> runStateUpdater.fork
      *> runServer
  }

  def run: URIO[ZIOAppArgs, Unit] =
    main
      .provideLayer(layer)
      .tapErrorCause(c => ZIO.logErrorCause("Critical error", c))
      .exitCode
      .flatMap(exit(_))

}
