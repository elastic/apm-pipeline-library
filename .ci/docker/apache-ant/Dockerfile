FROM openjdk:15-jdk

ARG ANT_VERSION=1.10.8
ARG BASE_URL=https://mirrors.ircam.fr/pub/apache/ant/binaries

RUN mkdir -p /usr/share/ant /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-ant.tar.gz ${BASE_URL}/apache-ant-${ANT_VERSION}-bin.tar.gz \
  && tar -xzf /tmp/apache-ant.tar.gz -C /usr/share/ant --strip-components=1 \
  && rm -f /tmp/apache-ant.tar.gz \
  && ln -s /usr/share/ant/bin/ant /usr/bin/ant

ENTRYPOINT ["ant"]
CMD ["-version"]
