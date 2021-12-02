FROM kibana-yarn:latest AS src

ARG GID=1001
ARG UID=1001
ARG REFSPEC=main

RUN git fetch --quiet --jobs 5 origin "${REFSPEC}" \
  && git checkout "origin/${REFSPEC}" -b "branch_run"

ENV HOME=/usr/share/kibana
ENV NODE_OPTIONS= --max-old-space-size=4096
ENV FORCE_COLOR=1
ENV BABEL_DISABLE_CACHE=true
ENV NVM_DIR=${HOME}/.nvm
RUN . "${NVM_DIR}/nvm.sh" \
  && nvm install $(cat .node-version) \
  && nvm alias default $(cat .node-version) \
  && yarn config set cache-folder ${HOME}/.yarn_cache \
  && npm install -g yarn-deduplicate \
  && yarn-deduplicate yarn.lock \
  && npm set progress=false \
  && yarn kbn bootstrap --prefer-offline --no-audit --link-duplicates

EXPOSE 5601
ENTRYPOINT ["/bin/bash", "-c"]
CMD ["yarn start -c /usr/share/kibana/config/kibana.yml --no-dev-config --no-optimizer"]


HEALTHCHECK --interval=10s --timeout=5s --start-period=1m --retries=300 CMD curl -sSL http://127.0.0.1:5601/login|grep -v 'Kibana server is not ready yet'
