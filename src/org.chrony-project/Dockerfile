FROM alpine:latest

EXPOSE 123/udp

RUN apk add --no-cache chrony

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
