ARG NODE_VERSION=16.11.1
FROM node:${NODE_VERSION} AS src

ARG GID=1001
ARG UID=1001
# 19846019a2841428b8564d9ad5dfd56334e41277
# main
# FIXME for PR the checkout and the fetch are diferents git fetch origin refs/pull/124994/head -> git checkout origin/PR/124994
ARG BRANCH=main

RUN groupadd -f --gid ${GID} kibana \
  && useradd --uid ${UID} --gid ${GID} --groups 0 --home-dir /usr/share/kibana --no-create-home kibana
# Bazel is installed at global level so we need permissions on /usr/local
RUN chown kibana:0 /usr/local /usr/local/bin /usr/local/lib /usr/local/share /usr/share

USER kibana
WORKDIR /usr/share
RUN git clone --depth 1 --branch main --single-branch --jobs 5 https://github.com/elastic/kibana.git \
  && git config --global user.email "none@example.com" \
  && git config --global user.name "None" \
  && git config --global --add remote.origin.fetch '+refs/pull/*/head:refs/remotes/origin/PR/*' \
  && git config --global --add remote.origin.fetch '+refs/heads/*:refs/remotes/origin/*'

WORKDIR /usr/share/kibana
RUN git fetch --depth 1 --jobs 5 origin "${BRANCH}" \
  && git checkout "${BRANCH}" -b "freeze_branch"

ENV HOME=/usr/share/kibana
ENV NVM_DIR=${HOME}/.nvm
ENV NODE_OPTIONS= --max-old-space-size=4096
ENV FORCE_COLOR=1
ENV BABEL_DISABLE_CACHE=false
ENV BAZEL_CACHE_MODE=read
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
RUN . "${NVM_DIR}/nvm.sh" \
  && nvm install $(cat .node-version) \
  && nvm alias default $(cat .node-version)
RUN . "${NVM_DIR}/nvm.sh" \
  && npm install -g yarn-deduplicate \
  && yarn-deduplicate yarn.lock \
  && npm set progress=false
RUN . "${NVM_DIR}/nvm.sh" \
  && yarn config set cache-folder ${HOME}/.yarn_cache \
  && yarn kbn bootstrap --prefer-offline --no-audit --link-duplicates

RUN . "${NVM_DIR}/nvm.sh" \
  && yarn kbn clean



FROM node:${NODE_VERSION}

ARG GID=1001
ARG UID=1001

RUN groupadd -f --gid ${GID} kibana \
  && useradd --uid ${UID} --gid ${GID} --groups 0 --home-dir /usr/share/kibana --no-create-home kibana \
  && chown kibana:0 /usr/local /usr/local/bin /usr/local/lib /usr/local/share /usr/share

USER kibana
ENV HOME=/usr/share/kibana
ENV NVM_DIR=${HOME}/.nvm
ENV NODE_OPTIONS= --max-old-space-size=4096
ENV FORCE_COLOR=1
ENV BABEL_DISABLE_CACHE=true

COPY --from=src ${HOME} ${HOME}
USER root
RUN chown kibana:0 /usr/local /usr/local/bin /usr/local/lib /usr/local/share /usr/share /usr/share/kibana

USER kibana
WORKDIR ${HOME}

EXPOSE 5601
ENTRYPOINT ["/bin/bash"]
CMD [". \"${NVM_DIR}/nvm.sh\" && bash"]
