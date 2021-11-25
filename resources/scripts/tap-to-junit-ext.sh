#!/usr/bin/env bash
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
set -eo pipefail

if [ -n "${PIPELINE_LOG_LEVEL}" ] && [ "${PIPELINE_LOG_LEVEL}" == "DEBUG" ] ; then
  set -x
fi

PATTERN=${1:?'Missing the pattern'}

for file in ${PATTERN} ; do
echo "Parsing ${file}"
filepath=$(dirname "$file")
name=$(basename "$file")
python3 -c "from itertools import count
with open('${file}') as file:
    for i in count():
        firstline = next(file, None)
        if firstline is None:
            break
        print(firstline)
        with open(f'${filepath}/{i}-${name}.ext', 'w') as out:
            out.write(firstline)
            for line in file:
                out.write(line)
                if line.startswith('running (cwd: ./'):
                    break" || true
done
