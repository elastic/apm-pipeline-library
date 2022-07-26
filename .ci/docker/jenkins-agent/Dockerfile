FROM docker.elastic.co/infra/jenkins-swarm:alma8

# Need to switch to the `root` user, as the default is `jenkins`.
USER root

# Install jq
RUN dnf install -qy jq && dnf -qy clean all

# Switch back to the `jenkins` user before
USER jenkins
