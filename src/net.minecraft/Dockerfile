#----------------------------------------------------------------
# https://www.minecraft.net/en-us/download/server
FROM alpine/curl:latest as downloader

ARG MINECRAFT_VERSION=95495a7f485eedd84ce928cef5e223b757d2f764/server.jar

RUN \
  curl -fsSLO "https://launcher.mojang.com/v1/objects/${MINECRAFT_VERSION}/server.jar" || \
  curl -fsSLO "https://piston-data.mojang.com/v1/objects/${MINECRAFT_VERSION}/server.jar"

#----------------------------------------------------------------
FROM openjdk:26-slim

EXPOSE 25565

COPY --from=downloader \
  /server.jar \
  /net.minecraft/server.jar

WORKDIR /net.minecraft
RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get autoremove -y && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  echo 'eula=true' > eula.txt

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
