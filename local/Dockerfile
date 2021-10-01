FROM docker.elastic.co/infra/jenkins:202109131610.ef2e7a6a5a14


COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt
