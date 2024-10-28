#!/bin/bash

rm -v *.jar
./gradlew clean fatJar && mv -v build/libs/kafka-request-tool-*.jar .
