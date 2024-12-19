FROM docker.io/library/openjdk:8-jre-slim-bullseye

ARG ARCHIVA_VERSION=2.2.10

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    curl \
    procps && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  curl -fsSL "https://archive.apache.org/dist/archiva/${ARCHIVA_VERSION}/binaries/apache-archiva-${ARCHIVA_VERSION}-bin.tar.gz" | tar zxf - --no-same-permissions --no-same-owner && \
  mv "apache-archiva-${ARCHIVA_VERSION}" '/org.apache.archiva'

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
