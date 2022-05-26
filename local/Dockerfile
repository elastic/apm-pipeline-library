FROM docker.elastic.co/infra/jenkins:202205181458.f1daa9ac6ec5


COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt
