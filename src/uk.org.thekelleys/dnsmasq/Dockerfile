# syntax = docker/dockerfile:latest

#----------------------------------------------------------------
FROM alpine:latest

EXPOSE 53/udp

ENTRYPOINT ["/usr/sbin/dnsmasq"]
CMD ["-k"]

RUN apk add --no-cache dnsmasq
