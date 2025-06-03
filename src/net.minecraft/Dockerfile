#----------------------------------------------------------------
# https://www.minecraft.net/en-us/download/server
FROM alpine/curl:latest as downloader

ARG MINECRAFT_VERSION=e6ec2f64e6080b9b5d9b471b291c33cc7f509733

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
