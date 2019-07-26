#!/usr/bin/env bash

# shellcheck disable=SC1091
source /usr/local/bin/bash_standard_lib.sh

# docker.elastic.co/observability-ci/apm-integration-testing-tests-rum
# docker.elastic.co/observability-ci/apm-integration-testing-tests-ruby-rails
# docker.elastic.co/observability-ci/apm-integration-testing-tests-python-flask
# docker.elastic.co/observability-ci/apm-integration-testing-tests-python-django
# docker.elastic.co/observability-ci/apm-integration-testing-tests-nodejs-express
# docker.elastic.co/observability-ci/apm-integration-testing-tests-java-spring
# docker.elastic.co/observability-ci/apm-integration-testing-tests-go-nethttp
# docker.elastic.co/observability-ci/apm-integration-testing-tests-dotnet
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-rum
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-ruby
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-python
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-node
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-java
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-go
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-frontend
# docker.elastic.co/observability-ci/apm-integration-testing-tests-opbeans-dotnet
# docker.elastic.co/observability-ci/apm-integration-testing-tests-apm-server

DOCKER_IMAGES="alpine:3.4
alpine:3.10.1
node:12-slim
node:12.7.0-stretch-slim
docker.elastic.co/observability-ci/yamllint
docker.elastic.co/observability-ci/shellcheck
python:3.7.4-alpine3.10
"

for di in ${DOCKER_IMAGES}
do
(retry 2 docker pull "${di}") || echo "Error pulling ${di} Docker image, we continue"
done
