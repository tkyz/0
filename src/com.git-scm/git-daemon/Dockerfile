# syntax = docker/dockerfile:latest

#----------------------------------------------------------------
FROM alpine:latest

EXPOSE 9418

ENTRYPOINT ["git", "daemon"]
CMD ["--export-all", "--base-path=/var/git", "/var/git"]

RUN apk --no-cache add git-daemon
