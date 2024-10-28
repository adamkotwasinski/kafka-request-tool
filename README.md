# kafka-request-tool

A tool for sending low-level Kafka requests.

## Building

`./gradlew fatJar` creates a fat jar that can be launched via `java -jar kafka-request-tool.jar`.

## Features

### API Versions

`java -jar kafka-request-tool.jar host localhost port 9092 apiversions`

Sends an API_VERSIONS request that tells us what request versions are used by the Kafka broker.

### Metadata

`java -jar kafka-request-tool.jar host localhost port 9092 metadata [topics]`

Fetches the metadata from the server.

Empty topic list returns all topics present in the cluster.
