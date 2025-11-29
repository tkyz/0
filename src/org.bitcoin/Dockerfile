#----------------------------------------------------------------
# https://github.com/bitcoin/bitcoin.git
FROM debian:latest as builder

ARG BITCOIN_VERSION=v30.0

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    pkg-config \
    git \
    g++ \
    cmake \
    libboost-dev \
    libevent-dev \
    libsqlite3-dev \
    capnproto \
    libcapnp-dev && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  git clone --depth 1 --branch "${BITCOIN_VERSION}" https://github.com/bitcoin/bitcoin.git && \
    cd bitcoin && \
      cmake -B build && \
      cmake --build build -j 8 && \
      cmake --install build && \
    cd .. && \
  rm -rf bitcoin

#----------------------------------------------------------------
FROM debian:latest

EXPOSE 8332 8333

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    libevent-2.1-7 \
    libevent-pthreads-2.1-7 \
    libevent-extra-2.1-7 && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

COPY --from=builder \
  /usr/local/bin \
  /org.bitcoin

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
