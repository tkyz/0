#----------------------------------------------------------------
# https://github.com/NetHack/NetHack.git
# https://github.com/jnethack/jnethack-alpha.git
FROM debian:latest as builder

ARG JNETHACK_VERSION=v3.6.7-0.1

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    bsdextrautils \
    groff \
    bison \
    flex \
    nkf \
    git \
    make \
    btyacc \
    gcc \
    libncurses-dev && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  git clone --depth 1 --branch "${JNETHACK_VERSION}" https://github.com/jnethack/jnethack-alpha.git && \
    cd jnethack-alpha && \
      sed \
        -i \
        -r \
        -e 's|^(HACKDIR=).*$|\1/org.jnethack|g' \
        sys/unix/hints/linux && \
      ./configure && \
      make all && \
      make install && \
    cd .. && \
  rm -rf jnethack-alpha

#----------------------------------------------------------------
FROM debian:latest

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    libncurses6 && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

COPY --from=builder \
  /org.jnethack \
  /org.jnethack

ENV NETHACKOPTIONS "kcode:u,!autopickup,color,disclose:+i +a +v +g +c,hilite_pet"

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
