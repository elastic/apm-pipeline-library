FROM docker.elastic.co/infra/jenkins-swarm:latest

# Need to switch to the `root` user, as the default is `jenkins`.
USER root

# As long it uses centos:8 there is a need to change the mirrors
RUN sed -i -e "s|mirrorlist=|#mirrorlist=|g" /etc/yum.repos.d/CentOS-*
RUN sed -i -e "s|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g" /etc/yum.repos.d/CentOS-*

# Install jq
RUN dnf install -qy jq

# Install wget
RUN dnf install -qy wget && dnf -qy clean all

# Install hub
RUN wget https://github.com/github/hub/releases/download/v2.14.2/hub-linux-amd64-2.14.2.tgz -O /tmp/hub.tgz \
  && mkdir /hub \
  && tar xvf /tmp/hub.tgz -C /hub --strip-components 1 \
  && ln -fs /hub/bin/hub /usr/local/bin/hub \
  && rm /tmp/hub.tgz

# Switch back to the `jenkins` user before
USER jenkins
