FROM debian:latest

EXPOSE 3142

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y \
    apt-cacher-ng && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  echo 'PassThroughPattern: .*'         >> /etc/apt-cacher-ng/acng.conf
# echo 'PassThroughPattern: ^(.*):443$' >> /etc/apt-cacher-ng/acng.conf

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
