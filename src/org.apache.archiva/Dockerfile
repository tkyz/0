# syntax = docker/dockerfile:latest

ARG tag=latest

#----------------------------------------------------------------
FROM curlimages/curl:latest as curl

ARG tag

WORKDIR /tmp

COPY "./tags/${tag}" .

RUN true \
  && source './env' \
  && curl --silent --show-error \
    --location "https://dlcdn.apache.org/archiva/${APACHE_ARCHIVA_VERSION}/binaries/apache-archiva-${APACHE_ARCHIVA_VERSION}-bin.tar.gz" | tar zxf - \
  && mv "apache-archiva-${APACHE_ARCHIVA_VERSION}" /home/org.apache.archiva

#----------------------------------------------------------------
FROM openjdk:8-jre as main

EXPOSE 8080

ENTRYPOINT ["/home/org.apache.archiva/bin/archiva"]
CMD ["console"]

RUN true \
  && apt update \
  && apt upgrade -y \
  && apt install -y procps \
  && apt clean \
  && rm -rf /var/lib/apt/lists/*

COPY --from=curl \
  /home/org.apache.archiva \
  /home/org.apache.archiva
