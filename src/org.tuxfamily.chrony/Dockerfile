# syntax = docker/dockerfile:latest

#----------------------------------------------------------------
FROM alpine:latest

EXPOSE 123/udp

ENTRYPOINT ["chronyd"]
CMD ["-d"]

RUN apk add --no-cache chrony
