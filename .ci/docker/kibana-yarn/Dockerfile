ARG NODE_VERSION=16.14.2
FROM node:${NODE_VERSION}-bullseye AS src

# 19846019a2841428b8564d9ad5dfd56334e41277
# main
# FIXME for PR the checkout and the fetch are diferents git fetch origin refs/pull/124994/head -> git checkout origin/PR/124994
ARG BRANCH=main

RUN chown node:0 /usr/local /usr/local/bin /usr/local/lib /usr/local/share

USER node

ENV HOME=/home/node
ENV NVM_DIR=${HOME}/.nvm
ENV NODE_OPTIONS= --max-old-space-size=4096
ENV FORCE_COLOR=1
ENV BABEL_DISABLE_CACHE=false
ENV BAZEL_CACHE_MODE=read
ENV DISABLE_BOOTSTRAP_VALIDATION=true

WORKDIR /home/node
RUN git clone --depth 1 --branch main --single-branch --jobs 5 https://github.com/elastic/kibana.git \
  && git config --global user.email "none@example.com" \
  && git config --global user.name "None"

WORKDIR /home/node/kibana
RUN git --version && echo 1
RUN git fetch --depth 1 --jobs 5 origin "${BRANCH}" \
  && cat .git/FETCH_HEAD \
  && git checkout "${BRANCH}" -b "freeze_branch" \
  && cat .git/FETCH_HEAD \
  && git log -1 FETCH_HEAD --pretty=%h \
  && cat .git/FETCH_HEAD

RUN git log -1 FETCH_HEAD --pretty=%h
RUN git merge-base HEAD FETCH_HEAD

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
RUN . "${NVM_DIR}/nvm.sh" \
  && nvm install $(cat .node-version) \
  && nvm alias default $(cat .node-version)

RUN . "${NVM_DIR}/nvm.sh" \
  && yarn kbn bootstrap --prefer-offline --no-audit --link-duplicates

RUN . "${NVM_DIR}/nvm.sh" \
  && yarn kbn clean

WORKDIR /home/node
RUN rm -fr kibana

FROM docker:20.10.14 AS docker
FROM docker/buildx-bin:0.8.1 AS buildx
FROM node:${NODE_VERSION}-bullseye

USER node
ENV HOME=/home/node
ENV NVM_DIR=${HOME}/.nvm
ENV NODE_OPTIONS= --max-old-space-size=4096
ENV FORCE_COLOR=1
ENV BABEL_DISABLE_CACHE=true

COPY --from=src ${HOME} ${HOME}
USER root
RUN chown node:0 /usr/local /usr/local/bin /usr/local/lib /usr/local/share
RUN echo ". \"${NVM_DIR}/nvm.sh\"" > ${HOME}/.bashrc

# Install Docker
COPY --from=docker /usr/local/bin/docker /usr/local/bin/docker
COPY --from=buildx /buildx /usr/libexec/docker/cli-plugins/docker-buildx
RUN curl -sSL "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
  && chmod +x /usr/local/bin/docker-compose
RUN curl -sSL https://github.com/docker/compose/releases/download/v2.2.3/docker-compose-linux-x86_64 -o /usr/libexec/docker/cli-plugins/docker-compose \
  && chmod +x /usr/libexec/docker/cli-plugins/docker-compose
RUN (docker version || true) && (docker-compose version || true) && docker buildx version && docker compose version

USER node
WORKDIR ${HOME}

EXPOSE 5601
ENTRYPOINT ["/bin/bash", "-l", "-c"]
CMD ["/bin/bash"]
