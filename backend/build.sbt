val scala3Version = "3.4.2"

Compile / mainClass := Some("tictactoe.Main")

lazy val root = project
  .in(file("."))
  .settings(
    name         := "tic-tac-toe-scala",
    version      := "0.2.0-SNAPSHOT",
    scalaVersion := scala3Version,
    run / fork   := true, // Makes exit codes work as expected
    scalacOptions ++= Seq(
      "-Xmax-inlines",
      "64"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core"    % Versions.circe,
      "io.circe" %% "circe-generic" % Versions.circe,
      "io.circe" %% "circe-parser"  % Versions.circe
    ),
    libraryDependencies ++= Seq(
      "dev.profunktor" %% "neutron-core"     % Versions.neutron,
      "dev.profunktor" %% "neutron-circe"    % Versions.neutron,
      "dev.profunktor" %% "neutron-function" % Versions.neutron
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"                      % Versions.zio,
      "dev.zio" %% "zio-json"                 % Versions.zioJson,
      "dev.zio" %% "zio-logging"              % Versions.zioLogging,
      "dev.zio" %% "zio-logging-slf4j-bridge" % Versions.zioLogging, // routes slf4j to zio-logging
      // with exclude reroutes other loggers (can be brought by other dependencies) to slf4j
      "org.slf4j" % "jcl-over-slf4j"   % Versions.slf4j,
      "org.slf4j" % "log4j-over-slf4j" % Versions.slf4j,
      "org.slf4j" % "jul-to-slf4j"     % Versions.slf4j,
      "dev.zio"  %% "zio-interop-cats" % Versions.zioCatsInterop,
      "io.d11"   %% "zhttp"            % Versions.zioHttp, // TODO: remove
      // "dev.zio"       %% "zio-http", // TODO: add
      "org.typelevel" %% "cats-core" % Versions.cats,
      "org.scalameta" %% "munit"     % Versions.munit % Test
    ),

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
