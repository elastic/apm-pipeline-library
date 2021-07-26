FROM docker.elastic.co/infra/jenkins:202107191510.c5edabb9486d


COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt
