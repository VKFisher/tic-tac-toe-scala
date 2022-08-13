# sbt project compiled with Scala 3

## Usage

- `sbt run` - run
- `sbt ~run` - rerun, watching files
- `sbt scalafix` – lint
- `sbt "~ scalafix; run"` - lint and rerun, watching files

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).
