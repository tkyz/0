# syntax = docker/dockerfile:latest

ARG tag=8

#----------------------------------------------------------------
FROM php:${tag}-fpm-alpine

EXPOSE 9000

RUN true \
  && apk update \
  && apk upgrade \
  && apk add --no-cache \
    libpq \
    libjpeg-turbo \
    libpng \
    gocryptfs \
    zip \
    imagemagick \
    freetype \
    oniguruma \
    ffmpeg \
  && apk add --no-cache --virtual .build-deps \
    libpq-dev \
    libjpeg-turbo-dev \
    libpng-dev \
    oniguruma-dev \
    freetype-dev \
  && docker-php-ext-configure gd \
    --with-jpeg \
    --with-freetype \
  && docker-php-ext-install \
    pdo_pgsql \
    pdo_mysql \
    mbstring \
    exif \
    gd \
  && apk del --purge .build-deps
