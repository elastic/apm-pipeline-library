FROM node:15.5.0-alpine3.12

RUN npm install github-label-sync@2.0.0 -g
WORKDIR /app
ENTRYPOINT [ "/usr/local/bin/github-label-sync" ]
