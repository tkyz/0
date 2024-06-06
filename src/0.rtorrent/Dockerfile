FROM debian:latest

RUN true \
  && apt-get update \
  && apt-get upgrade -y \
  && apt-get install -y \
    mktorrent \
    rtorrent \
    unar \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* \
  && mkdir -p \
    /.rtorrent

COPY .rtorrent.rc      /root/.rtorrent.rc
COPY rtorrent-event.sh /usr/local/bin/rtorrent-event.sh

ENTRYPOINT ["rtorrent"]
CMD ["-h"]
