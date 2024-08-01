# Tic-tac-toe

Co-created with [Vladimir Logachev](https://github.com/vladimirlogachev)

Scala 3, Elm 0.19.1, Apache Pulsar

A toy tic-tac-toe implementation, made to explore Scala 3 and concepts related to event sourcing

## Usage

- `docker compose up -dV --wait` - start dependencies

### Backend

- `sbt run` - run
- `sbt test` - test
- `sbt styleFix` - fix formatting and linting errors
- `sbt styleCheck` - check for formatting and linting errors
- `sbt dev` - allow compiler warnings to not fail the compilation
- `sbt dependencyUpdates` - list dependency updates
- `~reStart` - run; rerun the server on code changes (in a dirty way)

### Webapp

- `npm start` - run
