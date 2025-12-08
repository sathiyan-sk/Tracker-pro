#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
export PATH=$JAVA_HOME/bin:$PATH

cd /app
echo "Starting TrackerPro Spring Boot Application..."
echo "JAVA_HOME: $JAVA_HOME"
java -version

./mvnw spring-boot:run
