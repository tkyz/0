FROM docker.io/library/openjdk:8-jre-slim-bullseye

RUN true \
  && apt-get update \
  && apt-get upgrade -y \
  && apt-get install -y \
    procps curl \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* \
  && mkdir -p '/opt/org.apache.archiva' \
  && cd       '/opt/org.apache.archiva' \
  && curl -fsSL 'https://dlcdn.apache.org/archiva/2.2.10/binaries/apache-archiva-2.2.10-bin.tar.gz' | tar zxf - --strip-components 1 --no-same-permissions --no-same-owner

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
