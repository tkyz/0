FROM alpine:latest

EXPOSE 53/tcp 53/udp

RUN apk add --no-cache dnsmasq

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
