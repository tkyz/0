#----------------------------------------------------------------
# https://github.com/bitcoin/bitcoin.git
FROM debian:latest as builder

ARG BITCOIN_VERSION=v28.0

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    git \
    g++ \
    make \
    autoconf \
    pkg-config \
    bsdmainutils \
    libtool \
    libboost-dev \
    libevent-dev \
    libsqlite3-dev && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  git clone --depth 1 --branch "${BITCOIN_VERSION}" https://github.com/bitcoin/bitcoin.git && \
    cd bitcoin && \
      ./autogen.sh && \
      ./configure && \
      make && \
      make install && \
    cd .. && \
  rm -rf bitcoin

#----------------------------------------------------------------
FROM debian:latest

EXPOSE 8333 8334

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    libevent-2.1-7 \
    libevent-pthreads-2.1-7 \
    libsqlite3-0 && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

COPY --from=builder \
  /usr/local/bin \
  /org.bitcoin

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
