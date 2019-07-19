FROM node:12-slim

RUN apt-get update -qq -y \
  && apt-get install -qq -y --no-install-recommends git \
  && rm -rf /var/lib/apt/lists/*

RUN npm install github-release-notes -g

ENTRYPOINT [ "/usr/local/bin/gren" ]
