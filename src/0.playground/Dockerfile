# syntax = docker/dockerfile:latest

#----------------------------------------------------------------
#FROM openjdk:latest
FROM openjdk:18.0.2.1

ARG name
ARG version

COPY "./target/dependency/*.jar"           /usr/local/lib
COPY "./target/${name}-0.0.1-SNAPSHOT.jar" /usr/local/lib
#COPY "./target/${name}-${version}.jar"    /usr/local/lib # TODO: jar version

ENV CLASSPATH="/usr/local/lib/*"

ENTRYPOINT ["java", "Entrypoint"]
