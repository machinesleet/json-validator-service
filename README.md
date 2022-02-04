# json-validator-service

Akka based web service that wraps the [json-schema-validator](https://github.com/java-json-tools/json-schema-validator) to allow a user to upload JSON schemas, and validate JSON documents against schemas, defined by a schema id string.

Schemas may also be downloaded by their schema id string.

## Endpoints
```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```

## Run
The service is configured to run at localhost:8080, and can be started firstly via `sbt compile run`, then via `sbt run`

Pressing the `return` key will stop the server.
