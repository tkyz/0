FROM docker.io/library/openjdk:8-jre-slim-bullseye

RUN true \
  && apt-get update \
  && apt-get upgrade -y \
  && apt-get install -y \
    openssh-server \
    openssh-client \
    curl \
    gnupg2 \
  && curl -fsSL 'https://dlcdn.apache.org/bigtop/bigtop-3.2.1/repos/GPG-KEY-bigtop'            | gpg --dearmor --yes --output /etc/apt/trusted.gpg.d/org.apache.bigtop.gpg \
  && echo 'deb http://repos.bigtop.apache.org/releases/3.2.1/debian/11/$(ARCH) bigtop contrib' | tee                          /etc/apt/sources.list.d/org.apache.bigtop.list \
  && apt-get update \
  && apt-get install -y \
    $(apt list 2> /dev/null | grep ^hadoop    | cut -d '/' -f 1) \
    $(apt list 2> /dev/null | grep ^zookeeper | cut -d '/' -f 1 | grep -v ^zookeeperd) \
    $(apt list 2> /dev/null | grep ^hbase     | cut -d '/' -f 1) \
    $(apt list 2> /dev/null | grep ^phoenix   | cut -d '/' -f 1) \
    $(apt list 2> /dev/null | grep ^hive      | cut -d '/' -f 1) \
    $(apt list 2> /dev/null | grep ^spark     | cut -d '/' -f 1) \
    $(apt list 2> /dev/null | grep ^kafka     | cut -d '/' -f 1) \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* \
  && mkdir -p /run/sshd \
  && echo 'clientPortAddress=0.0.0.0' >> /etc/zookeeper/conf/zoo.cfg \
  && sed -i -e 's/localhost/0.0.0.0/g'   /etc/hadoop/conf/core-site.xml \
  && echo "export JAVA_HOME=${JAVA_HOME}" >  /usr/lib/bigtop-utils/bigtop-detect-javahome \
  && echo "export JAVA_HOME=${JAVA_HOME}" >> /etc/hadoop/conf/hadoop-env.sh \
  && echo "export JAVA_HOME=${JAVA_HOME}" >> /etc/hbase/conf/hbase-env.sh

COPY ./entrypoint /entrypoint
ENTRYPOINT ["/entrypoint"]
