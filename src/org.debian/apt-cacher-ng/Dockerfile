# syntax = docker/dockerfile:latest

#----------------------------------------------------------------
FROM debian:latest

EXPOSE 3142

COPY ./docker-entrypoint /docker-entrypoint
ENTRYPOINT ["/docker-entrypoint"]

RUN true \
  && apt update \
  && apt upgrade -y \
  && apt install -y apt-cacher-ng \
  && apt clean \
  && rm -rf /var/lib/apt/lists/* \
  && echo 'PassThroughPattern: ^(.*):443$' >> /etc/apt-cacher-ng/acng.conf
