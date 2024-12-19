FROM alpine:latest

EXPOSE 123/udp

RUN apk add --no-cache chrony

ENTRYPOINT ["chronyd"]
CMD ["-d"]
