# Tic-tac-toe

Co-created with [Vladimir Logachev](https://github.com/vladimirlogachev)

Scala 3, Elm 0.19.1, Apache Pulsar

A toy tic-tac-toe implementation, made to explore Scala 3 and concepts related to event sourcing

## Pre-requisites

- [sbt](https://www.scala-sbt.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

## Usage

- `docker compose up -dV --wait` - start dependencies

### Backend

```sh
cd backend
```

- `sbt run` - run
- `sbt test` - test
- `sbt styleFix` - fix formatting and linting errors
- `sbt styleCheck` - check for formatting and linting errors
- `sbt dev` - allow compiler warnings to not fail the compilation
- `~reStart` - run; rerun the server on code changes (in a dirty way)

### Webapp

```sh
cd frontend
```

- `npm start` - run
