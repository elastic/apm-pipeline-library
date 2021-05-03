FROM docker.elastic.co/infra/jenkins:202104291643.e55184166455


COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
