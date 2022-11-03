val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "tic-tac-toe-scala",
    version := "0.2.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-Werror",
      "-Xmax-inlines", "64"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % "0.14.1"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.8.0",
      "dev.zio" %% "zio" % "2.0.2",
      "dev.zio" %% "zio-json" % "0.3.0-RC10",
      "io.d11" %% "zhttp" % "2.0.0-RC10",
      "io.getquill" %% "quill-zio" % "4.6.0",
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0",
      "com.h2database" % "h2" % "2.1.214",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    ),

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
