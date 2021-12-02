#!/usr/bin/env bash

# shellcheck disable=SC1091
source /usr/local/bin/bash_standard_lib.sh

# docker.elastic.co/observability-ci/it_rum
# docker.elastic.co/observability-ci/it_ruby-rails
# docker.elastic.co/observability-ci/it_python-flask
# docker.elastic.co/observability-ci/it_python-django
# docker.elastic.co/observability-ci/it_nodejs-express
# docker.elastic.co/observability-ci/it_java-spring
# docker.elastic.co/observability-ci/it_go-nethttp
# docker.elastic.co/observability-ci/it_dotnet
# docker.elastic.co/observability-ci/it_opbeans-rum
# docker.elastic.co/observability-ci/it_opbeans-ruby
# docker.elastic.co/observability-ci/it_opbeans-python
# docker.elastic.co/observability-ci/it_opbeans-node
# docker.elastic.co/observability-ci/it_opbeans-java
# docker.elastic.co/observability-ci/it_opbeans-go
# docker.elastic.co/observability-ci/it_opbeans-frontend
# docker.elastic.co/observability-ci/it_opbeans-frontend_nginx
# docker.elastic.co/observability-ci/it_opbeans-dotnet
# docker.elastic.co/observability-ci/it_apm-server

DOCKER_IMAGES="alpine:3.4
alpine:3.10.1
node:12-slim
node:12.7.0-stretch-slim
python:3.7.4-alpine3.10
docker.elastic.co/beats/filebeat:7.15.2
docker.elastic.co/beats/metricbeat:7.15.2
docker.elastic.co/infra/jjbb
docker.elastic.co/observability-ci/codecov
docker.elastic.co/observability-ci/golang-mage
docker.elastic.co/observability-ci/gren
docker.elastic.co/observability-ci/shellcheck
docker.elastic.co/observability-ci/yamllint
ghcr.io/tcort/markdown-link-check:3.8.5
widerplan/jenkins-job-builder
github/super-linter:latest
"
if [ -x "$(command -v docker)" ]; then
  for di in ${DOCKER_IMAGES}
  do
  (retry 2 docker pull "${di}") || echo "Error pulling ${di} Docker image, we continue"
  done
fi

## Let's cache the maven dependencies
 ./mvnw clean install --batch-mode -DskipTests --fail-never
