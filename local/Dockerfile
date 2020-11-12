FROM docker.elastic.co/infra/jenkins:202010021728.bc28cd532cad

COPY configs/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
