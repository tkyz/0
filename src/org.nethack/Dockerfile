# syntax = docker/dockerfile:latest

ARG tag=latest

#----------------------------------------------------------------
FROM container.0/0:latest as builder

ARG tag

WORKDIR /tmp

COPY "./tags/${tag}" .

# https://github.com/NetHack/NetHack.git
# https://github.com/jnethack/jnethack-alpha.git
RUN true \
  && source './env' \
  && git clone --branch "${JNETHACK_VERSION}" https://github.com/jnethack/jnethack-alpha.git \
  && cd jnethack-alpha \
  && sed \
    -i \
    -r \
    -e 's,^(HACKDIR=).*$,\1/home/org.nethack/lib,' \
    -e 's,^(SHELLDIR=).*$,\1/home/org.nethack,' \
    -e 's,^(INSTDIR=).*$,\1/home/org.nethack/lib,' \
    -e 's,^(VARDIR=).*$,\1/home/org.nethack/lib,' \
    sys/unix/hints/linux \
  && ./configure \
  && make install \
  && mv \
    /root/nh/install/games/jnethack \
    /home/org.nethack

#----------------------------------------------------------------
FROM debian:latest as main

ENV NETHACKOPTIONS "kcode:u,!autopickup,color,disclose:+i +a +v +g +c,hilite_pet"
ENV PATH "${PATH}:/home/org.nethack"

ENTRYPOINT ["jnethack"]

RUN true \
  && apt update \
  && apt upgrade -y \
  && apt install -y libncurses6 \
  && apt clean \
  && rm -rf /var/lib/apt/lists/*

COPY --from=builder \
  /home/org.nethack \
  /home/org.nethack
