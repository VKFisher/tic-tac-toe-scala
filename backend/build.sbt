val scala3Version = "3.1.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "simple-scala",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-Werror"
    ), 

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.7.0",
      "dev.zio" %% "zio" % "2.0.0",
      "dev.zio" %% "zio-json" % "0.3.0-RC10",
      "io.d11" %% "zhttp" % "2.0.0-RC10",
      "io.getquill" %% "quill-zio" % "4.3.0",
      "io.getquill" %% "quill-jdbc-zio" % "4.3.0",
      "com.h2database" % "h2" % "2.1.214",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    ),

    // Scalafix
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
