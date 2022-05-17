FROM docker/buildx-bin:0.8.1 AS buildx
FROM docker:20.10.14 AS docker

RUN apk --no-cache add curl
# Install Bbuildx and Docker compose
COPY --from=buildx /buildx /usr/libexec/docker/cli-plugins/docker-buildx
RUN curl -sSL "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
  && chmod +x /usr/local/bin/docker-compose
RUN curl -sSL https://github.com/docker/compose/releases/download/v2.2.3/docker-compose-linux-x86_64 -o /usr/libexec/docker/cli-plugins/docker-compose \
  && chmod +x /usr/libexec/docker/cli-plugins/docker-compose
RUN (docker version || true) && (docker-compose version || true) && docker buildx version && docker compose version && docker buildx ls

COPY config.json /root/.docker/config.json
COPY docker-credential-env /usr/bin/docker-credential-env

ENTRYPOINT ["/bin/sh", "-l", "-c"]
CMD ["/bin/sh"]
