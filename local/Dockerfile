FROM docker.elastic.co/infra/jenkins:202111081526.13f733a5d475


COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt
