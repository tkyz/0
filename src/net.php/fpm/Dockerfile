FROM php:fpm-alpine

EXPOSE 9000

RUN \
  apk update && \
  apk upgrade && \
  apk add --no-cache \
    php84-pdo_sqlite \
    php84-sqlite3 \
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
  apk del --purge .build && \
  sed -i 's/www-data/root/g' '/usr/local/etc/php-fpm.d/www.conf'

ENTRYPOINT ["php-fpm", "--allow-to-run-as-root"]
