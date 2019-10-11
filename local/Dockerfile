FROM docker.elastic.co/infra/jenkins:201910101902.a5fcf0cdf9cb

COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
