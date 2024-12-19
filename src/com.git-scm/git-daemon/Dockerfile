FROM alpine:latest

EXPOSE 9418

RUN apk add --no-cache git-daemon

ENTRYPOINT ["git", "daemon"]
CMD ["--export-all", "--base-path=/var/git", "/var/git"]
