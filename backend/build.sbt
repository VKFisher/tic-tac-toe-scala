val scala3Version = "3.3.1"

Compile / mainClass := Some("tictactoe.Main")

lazy val root = project
  .in(file("."))
  .settings(
    name := "tic-tac-toe-scala",
    version := "0.2.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-Werror",
      "-Xmax-inlines",
      "64"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5"
    ),
    libraryDependencies ++= Seq(
      "dev.profunktor" %% "neutron-core" % "0.7.2",
      "dev.profunktor" %% "neutron-circe" % "0.7.2",
      "dev.profunktor" %% "neutron-function" % "0.7.2"
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.13",
      "dev.zio" %% "zio-json" % "0.4.2",
      "dev.zio" %% "zio-logging" % "2.1.11",
      "dev.zio" %% "zio-interop-cats" % "23.0.0.0",
      "org.typelevel" %% "cats-core" % "2.9.0",
      "io.d11" %% "zhttp" % "2.0.0-RC10",
      "io.getquill" %% "quill-zio" % "4.6.0.1",
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0.1",
      "com.h2database" % "h2" % "2.1.214",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    ),

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
