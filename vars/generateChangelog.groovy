// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**

Generate a CHANGELOG.md

* user: The GitHub user the repo belongs to. (Default: elastic)
* repo: The GitHub repo to generate the CHANGELOG for. If this
        is not present, the `REPO_NAME` environment variable is
        used.

When this call is made, the job will contain a `CHANGELOG.md` file
as an archive.

This module uses settings which are defined in a configuration file
in the root of the repository for which the changelog is being
generated. This file should be named `.github_changelog_generator`

For more details, see the full documentation:

https://github.com/github-changelog-generator/github-changelog-generator

**/

def call(Map params = [:]) {
    def user = params.get('user', 'elastic')
    def repo = params.get('repo', env."REPO_NAME")
    def token = getGithubToken()
    if (!repo) {
        error "Must provide `repo` argument to this step or set \$REPO_NAME in the environment"
    }

    withEnvMask(vars: [
        [var: "GITHUB_token", password: token]    ]){
            sh """
                docker run --name tmp_changelog_instance \
                -v ${env.PWD}:/usr/local/src/your-app ferrarimarco/github-changelog-generator \
                --project ${repo} \
                --user ${user} \
                --token ${GITHUB_token}

                docker cp tmp_changelog_instance:/usr/local/src/your-app/CHANGELOG.md ${env.WORKSPACE}/CHANGELOG.md
                docker rm -f tmp_changelog_instance
                """
        }
    archiveArtifacts artifacts: "CHANGELOG.md"
}
