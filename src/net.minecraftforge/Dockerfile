#----------------------------------------------------------------
# https://files.minecraftforge.net/net/minecraftforge/forge/
# https://www.minecraft.net/ja-jp/download/server

FROM docker.io/library/debian:latest

ARG OPENJDK_URL='https://download.java.net/java/GA/jdk25.0.2/b1e0dfa218384cb9959bdcb897162d4e/10/GPL/openjdk-25.0.2_linux-x64_bin.tar.gz'
ARG FORGE_URL='https://maven.minecraftforge.net/net/minecraftforge/forge/26.1.2-64.0.0/forge-26.1.2-64.0.0-installer.jar'

EXPOSE 25565

ENV PATH="/org.openjdk/bin:${PATH}"

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y curl && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  mkdir -p '/org.openjdk' && \
    cd '/org.openjdk' && \
    curl -fsSL "${OPENJDK_URL}"                                         | tar zxf - --strip-components 1 --no-same-permissions --no-same-owner && \
  mkdir -p '/net.minecraft' && \
    cd '/net.minecraft' && \
    curl -fsSL  'https://launcher.mojang.com/download/Minecraft.tar.gz' | tar zxf - --strip-components 1 --no-same-permissions --no-same-owner && \
    curl -fsSLO "https://launcher.mojang.com/v1/objects/97ccd4c0ed3f81bbb7bfacddd1090b0c56f9bc51/server.jar" && \
  mkdir -p '/net.minecraftforge' && \
    cd '/net.minecraftforge' && \
    curl -fsSL "${FORGE_URL}" -o 'forge-installer.jar' && \
    java -jar 'forge-installer.jar' nogui --installServer && \
    echo 'eula=true' > './eula.txt'

COPY './entrypoint' '/entrypoint'
ENTRYPOINT ["/entrypoint"]
