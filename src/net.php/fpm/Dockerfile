FROM php:fpm-alpine

EXPOSE 9000

RUN \
  apk update && \
  apk upgrade && \
  apk add --no-cache \
    libpq \
    libjpeg-turbo \
    libpng \
    gocryptfs \
    zip \
    imagemagick \
    freetype \
    oniguruma \
    ffmpeg && \
  apk add --no-cache --virtual .build \
    libpq-dev \
    libjpeg-turbo-dev \
    libpng-dev \
    oniguruma-dev \
    freetype-dev && \
  docker-php-ext-configure \
    gd \
    --with-jpeg \
    --with-freetype && \
  docker-php-ext-install \
    pdo_pgsql \
    pdo_mysql \
    mbstring \
    exif \
    gd && \
  apk del --purge .build
