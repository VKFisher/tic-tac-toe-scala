# Tic-tac-toe

Co-created with [Vladimir Logachev](https://github.com/vladimirlogachev)

Scala 3, Elm 0.19.1, Apache Pulsar

A toy tic-tac-toe implementation, made to explore Scala 3 and concepts related to event sourcing

## Usage

- `docker compose up -dV --wait` - start dependencies

### Backend

- `sbt run` - run
- `sbt test` - test
- `sbt scalafix` - lint
- `sbt ~reStart` - run; watch files
- `sbt "~ scalafix; test; reStart"` - lint, test and run; watch files

### Webapp

- `npm start` - run
