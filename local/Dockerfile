FROM docker.elastic.co/infra/jenkins:201908072008.10f20c0fb6e6

COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
