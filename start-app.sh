#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
export PATH=$JAVA_HOME/bin:$PATH
cd /app
./mvnw spring-boot:run
