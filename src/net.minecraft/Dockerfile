#----------------------------------------------------------------
FROM alpine/curl:latest as downloader

ARG MINECRAFT_VERSION=4707d00eb834b446575d89a61a11b5d548d8c001

RUN \
  curl -fsSLO "https://launcher.mojang.com/v1/objects/${MINECRAFT_VERSION}/server.jar" || \
  curl -fsSLO "https://piston-data.mojang.com/v1/objects/${MINECRAFT_VERSION}/server.jar"

#----------------------------------------------------------------
FROM openjdk:21-jdk-slim

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
