#----------------------------------------------------------------
FROM alpine/curl:latest as downloader

ARG ARCHIVA_VERSION=2.2.10

RUN \
  curl -fsSL "https://archive.apache.org/dist/archiva/${ARCHIVA_VERSION}/binaries/apache-archiva-${ARCHIVA_VERSION}-bin.tar.gz" | tar zxf - --no-same-permissions --no-same-owner && \
  mv "apache-archiva-${ARCHIVA_VERSION}" '/org.apache.archiva'

#----------------------------------------------------------------
FROM openjdk:8-jre-slim

COPY --from=downloader \
  /org.apache.archiva \
  /org.apache.archiva

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    procps && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
