FROM alpine:latest

EXPOSE 9418

RUN apk add --no-cache git-daemon

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
