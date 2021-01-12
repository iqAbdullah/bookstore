#! /bin/bash

exec mvn clean install -DskipTests -Dspring.profiles.active=docker
exec java -jar -Dspring.profiles.active=docker target/book-store-0.0.1-SNAPSHOT.jar
