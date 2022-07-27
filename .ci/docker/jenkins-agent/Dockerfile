FROM docker.elastic.co/infra/jenkins-swarm:alma8

# Need to switch to the `root` user, as the default is `jenkins`.
USER root

# Install jq
RUN dnf install -qy jq

# Install wget
RUN dnf install -qy wget

# Install hub
RUN wget https://github.com/github/hub/releases/download/v2.14.2/hub-linux-amd64-2.14.2.tgz -O /tmp/hub.tgz \
  && mkdir /hub \
  && tar xvf /tmp/hub.tgz -C /hub --strip-components 1 \
  && ln -fs /hub/bin/hub /usr/local/bin/hub \
  && rm /tmp/hub.tgz

# Install gh
RUN dnf -qy install 'dnf-command(config-manager)' \
  && dnf config-manager --add-repo https://cli.github.com/packages/rpm/gh-cli.repo \
  && dnf install -qy gh \
  && dnf -qy clean all

# Install gvm
RUN curl -L -o /usr/local/bin/gvm https://github.com/andrewkroh/gvm/releases/download/v0.3.0/gvm-linux-amd64 \
  && chmod 755 /usr/local/bin/gvm

# Switch back to the `jenkins` user before
USER jenkins
