ARG NODE_VERSION=16.11.1
FROM node:${NODE_VERSION} AS src

ARG GID=1001
ARG UID=1001
ARG REFSPEC=master

RUN mkdir -p /usr/share/kibana
WORKDIR /usr/share/kibana
RUN groupadd -f --gid ${GID} kibana \
  && useradd --uid ${UID} --gid ${GID} --groups 0 --home-dir /usr/share/kibana --no-create-home kibana \
  && chown -R kibana:0 /usr/share/kibana
# Bazel is installed at global level so we need permissions on /usr/local
RUN chown -R kibana:0 /usr/local

USER kibana
RUN git config --global user.email "none@example.com" \
  && git config --global user.name "None" \
  && git config --global --add remote.origin.fetch '+refs/pull/*/head:refs/remotes/origin/PR/*' \
  && git config --global --add remote.origin.fetch '+refs/heads/*:refs/remotes/origin/*' \
  && git init \
  && git remote add origin https://github.com/elastic/kibana.git \
  && git fetch --all --quiet --jobs 5 \
  && git checkout "origin/${REFSPEC}" -b "freeze_branch"

ENV HOME=/usr/share/kibana
ENV NVM_DIR=${HOME}/.nvm
ENV NODE_OPTIONS= --max-old-space-size=4096
ENV FORCE_COLOR=1
ENV BABEL_DISABLE_CACHE=true
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
RUN . "${NVM_DIR}/nvm.sh"; \
  nvm install $(cat .node-version)
RUN npm install -g yarn-deduplicate \
  && yarn-deduplicate yarn.lock \
  && npm set progress=false
RUN yarn config set cache-folder ${HOME}/.yarn_cache \
  && yarn kbn bootstrap --prefer-offline --no-audit --link-duplicates

EXPOSE 5601
ENTRYPOINT ["/bin/bash", "-c"]
CMD [". \"${NVM_DIR}/nvm.sh\" && yarn start -c /usr/share/kibana/config/kibana.yml --no-dev-config"]


HEALTHCHECK --interval=10s --timeout=5s --start-period=1m --retries=300 CMD curl -sSL http://127.0.0.1:5601/login|grep -v 'Kibana server is not ready yet'
