# syntax = docker/dockerfile:latest

#----------------------------------------------------------------
FROM debian:latest

SHELL ["/bin/bash", "-c"]

ENTRYPOINT ["/.bin/entrypoint"]
# CMD ["/bin/bash", "-l"]

RUN true \
  && cd / \
  && apt update \
  && apt upgrade -y \
  && apt install -y curl \
  && ( curl https://setup.0 || curl https://raw.githubusercontent.com/tkyz/0/main/setup ) | bash \
  && git checkout . \
  && source /etc/profile.d/0.sh \
  && /.bin/deps \
  && apt clean \
  && rm -rf /var/lib/apt/lists/*

RUN touch "/.$(date --utc "+%Y%m%d_%H%M%S_%N")"
