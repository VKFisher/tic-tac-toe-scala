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
      "io.circe" %% "circe-core"    % "0.14.9",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser"  % "0.14.9"
    ),
    libraryDependencies ++= Seq(
      "dev.profunktor" %% "neutron-core"     % "0.8.0",
      "dev.profunktor" %% "neutron-circe"    % "0.8.0",
      "dev.profunktor" %% "neutron-function" % "0.8.0"
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"              % "2.1.6",
      "dev.zio" %% "zio-json"         % "0.7.1",
      "dev.zio" %% "zio-logging"      % "2.3.0",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.2",
      "io.d11"  %% "zhttp"            % "2.0.0-RC11", // TODO: remove
      // "dev.zio"       %% "zio-http"         % "3.0.0-RC9", // TODO: add
      "org.typelevel" %% "cats-core"      % "2.12.0",
      "io.getquill"   %% "quill-zio"      % "4.8.5",
      "io.getquill"   %% "quill-jdbc-zio" % "4.8.5",
      "com.h2database" % "h2"             % "2.3.230",
      "org.scalameta" %% "munit"          % "1.0.0" % Test
    ),

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
