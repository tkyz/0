# syntax = docker/dockerfile:latest

ARG tag=4.8.30

#----------------------------------------------------------------
FROM container.0/0:latest as builder

ARG tag

WORKDIR /tmp

COPY "./tags/${tag}" .

RUN true \
  && source './env' \
  && curl --silent --show-error \
    --location "http://download.oracle.com/berkeley-db/db-${BERKELEYDB_VERSION}.tar.gz" | tar zxf - \
  && cd "db-${BERKELEYDB_VERSION}/build_unix" \
  && sed -i -r -e 's#^ac_default_prefix=/usr/local/BerkeleyDB\.[0-9\.]+$#ac_default_prefix=/usr/local/berkeleydb#g' ../dist/configure \
  && sed -i    -e 's/__atomic_compare_exchange(/__atomic_compare_exchange_patch(/g'                                 ../dbinc/atomic.h \
  && ../dist/configure \
    --enable-cxx \
  && make \
  && make install

#----------------------------------------------------------------
#FROM alpine:latest as main

#ENV LD_LIBRARY_PATH "${LD_LIBRARY_PATH}:/usr/local/berkeleydb/lib"

#COPY --from=builder \
#  /usr/local/berkeleydb \
#  /usr/local/berkeleydb
