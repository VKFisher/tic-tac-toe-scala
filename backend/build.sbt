val scala3Version = "3.4.2"

Compile / mainClass := Some("tictactoe.Main")

lazy val root = project
  .in(file("."))
  .settings(
    name         := "tic-tac-toe-scala",
    version      := "0.2.0-SNAPSHOT",
    scalaVersion := scala3Version,
    run / fork   := true, // Makes exit codes work as expected
    scalacOptions ++= Seq("-Xmax-inlines", "64"),
    // json parsing
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core"    % Versions.circe,
      "io.circe" %% "circe-generic" % Versions.circe,
      "io.circe" %% "circe-parser"  % Versions.circe
    ),
    // pulsar
    libraryDependencies ++= Seq(
      "dev.profunktor"   %% "neutron-core"      % Versions.neutron,
      "org.apache.pulsar" % "pulsar-client-api" % Versions.pulsarClient
    ),
    // logging
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-logging"              % Versions.zioLogging,
      "dev.zio" %% "zio-logging-slf4j-bridge" % Versions.zioLogging, // routes slf4j to zio-logging
      // Note: there three modules reroute loggers that can be brought by other dependencies to slf4j
      // they do this by replacing jcl, log4j and jul dependencies
      // these are not used directly in the code
      "org.slf4j" % "jcl-over-slf4j"   % Versions.slf4j,
      "org.slf4j" % "log4j-over-slf4j" % Versions.slf4j,
      "org.slf4j" % "jul-to-slf4j"     % Versions.slf4j
    ),
    // fp fighting arena
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"              % Versions.zio,
      "dev.zio"       %% "zio-interop-cats" % Versions.zioCatsInterop,
      "dev.zio"       %% "zio-streams"      % Versions.zioStreams,
      "org.typelevel" %% "cats-core"        % Versions.cats,
      "org.typelevel" %% "cats-effect"      % Versions.catsEffect,
      "co.fs2"        %% "fs2-core"         % Versions.fs2
    ),
    // http
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % Versions.zioHttp
    ),
    // tests
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % Versions.munit
    ).map(_ % Test),

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
