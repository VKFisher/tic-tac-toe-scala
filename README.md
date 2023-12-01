# Tic-tac-toe

Scala 3.3.1, Elm 0.19.1, Apache Pulsar

A toy tic-tac-toe implementation, made to explore Scala 3 and concepts related to event sourcing

Co-created with @vladimirlogachev

## Usage

- `docker compose up -dV pulsar` - start dependencies

### Backend

- `sbt run` - run
- `sbt test` - test
- `sbt scalafix` - lint
- `sbt ~reStart` - run; watch files
- `sbt "~ scalafix; test; reStart"` - lint, test and run; watch files

### Webapp

- `npm start` - run
