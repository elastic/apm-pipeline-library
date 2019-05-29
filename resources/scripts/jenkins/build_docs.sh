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

DOCS_DIR=${1:-?}

if [ -z "${ELASTIC_DOCS}" -o ! -d "${ELASTIC_DOCS}" ]; then
  echo "ELASTIC_DOCS is not defined, it should point to a folder where you checkout https:#github.com/elastic/docs.git."
  echo "You also can define BUILD_DOCS_ARGS for aditional build options."
  exit 1
fi

${ELASTIC_DOCS}/build_docs.pl --chunk=1 ${BUILD_DOCS_ARGS} --doc ${DOCS_DIR}/index.asciidoc -out ${DOCS_DIR}/html
