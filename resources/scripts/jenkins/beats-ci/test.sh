#!/usr/bin/env bash
# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

set +x
pip install virtualenv
virtualenv venv
# shellcheck disable=SC1091
source venv/bin/activate
pip install testinfra
set -x

## Whether to run the specific test-infra packer cache test suite.
PACKER=${1:-false}

## Run test-infra and trap error to notify when required
{ py.test -v \
    test-infra/beats-ci/test_installed_tools.py \
    test-infra/beats-ci/test_installed_tools_docker.py \
    --junit-xml=target/junit-test-infra.xml; \
  er="$?"; } || true
err="${er}"

if [ "${PACKER}" = "true" ] ; then
  { py.test -v \
      test-infra/beats-ci/test_packer.py \
      --junit-xml=target/junit-test-packer.xml; \
    er="$?"; } || true
  if [ $er -gt 0 ] ; then
    err="${er}"
  fi
fi

### https://docs.pytest.org/en/latest/usage.html#possible-exit-codes
case "$err" in
0) echo success ;;
1) echo fail ;;
*) exit $err ;;
esac
