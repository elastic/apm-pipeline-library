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
# Script that checkout the given repository and
# search for recent changes in the last x days for the given
# file. If no then it will create a template email to be sent
#
# Requirements:
#  - git, in order to clone the repository
#  - gh, in order to send the existing list of open PRs.
#

set -euo pipefail

REPO_URL=${1:?'Missing the GitHub repo URL'}
BRANCH=${2:?'Missing the branch'}
DAYS=${3:?'Missing the days since the file has not changed result'}

EMAIL_FILE=email.txt
if [ -e ${EMAIL_FILE} ] ; then
  rm ${EMAIL_FILE}
fi

echo "1. Clone repo $REPO_URL for $BRANCH"
git clone "$REPO_URL" --branch "$BRANCH" "$BRANCH"

cd "$BRANCH"

echo "2. Are there any recent changes in the last $DAYS days"
if git --no-pager \
    log --pretty=format: \
      --name-only \
      --since="${DAYS} days ago" \
    | grep 'testing/environments/snapshot-oss.yml' ; then

  echo 'There are recent changes, so nothing to be reported'
else

  echo 'There are no recent changes.'
cat <<EOT > ../${EMAIL_FILE}
Just wanted to share with you that the Elastic Stack version for the ${BRANCH} branch has not been updated for a while ( > ${DAYS} days).

Those bumps are automatically merged if it passes the CI checks. Otherwise, it might be related to some problems with the
Integrations Tests.

EOT

  echo "3. List the open Pull Requests targeting $BRANCH"
  gh pr list \
    --search "is:open is:pr author:apmmachine base:$BRANCH" \
    --json url,createdAt \
    --template '{{range .}}{{tablerow .url (.createdAt | timeago)}}{{end}}' >> ../${EMAIL_FILE}

cat <<EOT >> ../${EMAIL_FILE}

If any of the existing PRs are obsolete please close them.

This message was automatically generated by https://apm-ci.elastic.co/job/apm-shared/job/apm-schedule-weekly

Thanks
EOT

  echo "4. Email body created (see email.txt)."
fi
