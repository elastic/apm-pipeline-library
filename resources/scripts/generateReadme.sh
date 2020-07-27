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
#
# somple script to glue all steps help in a single README.md file
#
#

if [ $# -lt 1 ]; then
  echo "usage: ${0} folder"
  exit 1
fi

FOLDER="${1}"
README="${FOLDER}/README.md"
echo "FOLDER=${1}"
echo "README=${FOLDER}/README.md"

{
  echo "<!-- markdownlint-disable -->"
  echo "# Steps Documentation"
} > "${README}"

for i in "${FOLDER}"/*.txt
do
  echo "Procesing ${i}"
  step=$(basename "${i}" .txt)
  {
    echo "## ${step}"
    cat "$i"
    echo ""
  } >> "${README}"
done

echo "<!-- markdownlint-restore -->" >> "${README}"
