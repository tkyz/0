FROM alpine:latest

EXPOSE 8080 9418

RUN \
  apk add --no-cache \
    git-daemon \
    git-gitweb \
    perl \
    perl-cgi \
    lighttpd \
    highlight

COPY ./gitweb.conf   /etc/gitweb.conf
COPY ./lighttpd.conf /etc/lighttpd/lighttpd.conf

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
