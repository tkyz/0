FROM alpine:latest

RUN \
  apk update \
  && apk upgrade \
  && apk add \
    bash \
    mktorrent \
    rtorrent \
  && mkdir -p \
    /.rtorrent

COPY .rtorrent.rc      /root/.rtorrent.rc
COPY entrypoint        /entrypoint
COPY rtorrent-event.sh /usr/local/bin/rtorrent-event.sh

ENTRYPOINT ["/entrypoint"]
CMD ["-h"]
