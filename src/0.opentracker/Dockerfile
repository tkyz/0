FROM alpine:latest

RUN true \
  && apk add --no-cache \
    opentracker \
  && mkdir -p \
    /.opentracker

ENTRYPOINT ["opentracker"]
CMD ["--help"]
