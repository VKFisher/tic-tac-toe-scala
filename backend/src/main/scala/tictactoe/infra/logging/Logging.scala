package tictactoe.infra.logging

import java.nio.file.Path

import zio._
import zio.logging._
import zio.logging.slf4j.bridge.Slf4jBridge

object Logging {

  private val withAdditionalInfo: LogFormat => LogFormat = format =>
    (
      format
        |-| LogFormat.label("file", LogFormat.enclosingClass).color(LogColor.WHITE)
        |-| LogFormat.label("line", LogFormat.traceLine).color(LogColor.WHITE)
    )

  private val defaultFormat = withAdditionalInfo(LogFormat.default)
  private val coloredFormat = withAdditionalInfo(LogFormat.colored)

  private val coloredConsole: LogLevel => ZLayer[Any, Nothing, Any] = { logLevel =>
    consoleLogger(
      ConsoleLoggerConfig(
        format = coloredFormat,
        filter = LogFilter.LogLevelByNameConfig(logLevel)
      )
    )
  }

  private val jsonConsole: LogLevel => ZLayer[Any, Nothing, Any] = { logLevel =>
    consoleJsonLogger(
      ConsoleLoggerConfig(
        format = defaultFormat,
        filter = LogFilter.LogLevelByNameConfig(logLevel)
      )
    )
  }

  private val defaultFile: String => LogLevel => ZLayer[Any, Nothing, Any] = { filePath => logLevel =>
    fileLogger(
      FileLoggerConfig(
        destination = Path.of(filePath),
        format = defaultFormat,
        filter = LogFilter.LogLevelByNameConfig(logLevel)
      )
    )
  }

  val defaultLoggingSetup: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers
      >>> jsonConsole(LogLevel.Info)
      >+> Slf4jBridge.initialize

  def devLoggingSetup(
      logLevel: LogLevel,
      removeDefaultLoggers: Boolean,
      logToFile: Option[String]
  ): ZLayer[Any, Any, Any] = {
    (
      (if (removeDefaultLoggers) Runtime.removeDefaultLoggers else ZLayer.empty)
        >>> coloredConsole(logLevel)
        >>> logToFile.map(fp => defaultFile(fp)(logLevel)).getOrElse(ZLayer.empty)
        >+> Slf4jBridge.initialize
    )
  }

}
