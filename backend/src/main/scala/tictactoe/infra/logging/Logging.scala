package tictactoe.infra.logging

import zio._
// import zio.logging.* // TODO: enable

object Logging {
  // TODO: enable
  // private val jsonConsole: LogLevel => ZLayer[Any, Nothing, Unit] = { logLevel =>
  //   consoleJsonLogger(
  //     ConsoleLoggerConfig(
  //       format = LogFormat.default,
  //       filter = LogFilter.logLevel(logLevel)
  //     )
  //   )
  // }

  // TODO: enable
  // private val coloredConsole: LogLevel => ZLayer[Any, Nothing, Any] = {
  //   logLevel =>
  //     consoleLogger(
  //       ConsoleLoggerConfig(
  //         format = LogFormat.colored
  //           |-| LogFormat
  //             .label("file", LogFormat.enclosingClass)
  //             .color(LogColor.WHITE)
  //           |-| LogFormat
  //             .label("line", LogFormat.traceLine)
  //             .color(LogColor.WHITE),
  //         filter = LogFilter.logLevel(logLevel)
  //       )
  //     )
  // }

  val defaultLoggingSetup: ZLayer[Any, Nothing, Unit] = (
    // TODO: add location to json console logs
    Runtime.removeDefaultLoggers
      // >>> jsonConsole(LogLevel.Info)
      // >+> Slf4jBridge.initialize // TODO: enable
  )

  val devLoggingSetup: LogLevel => ZLayer[Any, Any, Any] = { logLevel =>
    Runtime.removeDefaultLoggers /* >>> coloredConsole(logLevel) */ // TODO: enable
  }

}
