#----------------------------------------------------------------
# https://github.com/ethereum/go-ethereum.git
FROM golang:latest as builder

ARG ETHEREUM_VERSION=v1.16.7

WORKDIR /
RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  git clone --depth 1 --branch "${ETHEREUM_VERSION}" https://github.com/ethereum/go-ethereum.git && \
    cd go-ethereum && \
      make all && \
      mv ./build/bin /org.ethereum && \
    cd .. && \
  rm -rf go-ethereum

#----------------------------------------------------------------
FROM debian:latest

EXPOSE 30303

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

COPY --from=builder \
  /org.ethereum \
  /org.ethereum

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
