FROM docker.elastic.co/infra/jenkins-swarm:latest

# Need to switch to the `root` user, as the default is `jenkins`.
USER root

# As long it uses centos:8 there is a need to change the mirrors
RUN sed -i -e "s|mirrorlist=|#mirrorlist=|g" /etc/yum.repos.d/CentOS-*
RUN sed -i -e "s|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g" /etc/yum.repos.d/CentOS-*

# Install jq
RUN dnf install -qy jq && dnf -qy clean all

# Switch back to the `jenkins` user before
USER jenkins
