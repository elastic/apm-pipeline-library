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
  Run the pre-commit for the given commit if provided and generates the JUnit
  report if required

  preCommit(junit: false)

  preCommit(commit: 'abcdefg')

  preCommit(commit: 'abcdefg', credentialsId: 'ssh-credentials-xyz')

  preCommit(registry: 'docker.elastic.co', secretRegistry: 'secret/apm-team/ci/docker-registry/prod')
*/
def call(Map params = [:]) {
  def junitFlag = params.get('junit', true)
  def commit = params.get('commit', env.GIT_BASE_COMMIT)
  def credentialsId = params.get('credentialsId', 'f6c7695a-671e-4f4f-a331-acdce44ff9ba')
  def registry = params.get('registry', 'docker.elastic.co')
  def secretRegistry = params.get('secretRegistry', 'secret/observability-team/ci/docker-registry/prod')

  if (!commit?.trim()) {
    commit = env.GIT_BASE_COMMIT ?: error('preCommit: git commit to compare with is required.')
  }

  def reportFileName = 'pre-commit.out'

  sshagent([credentialsId]) {

    def newHome = env.HOME ?: env.WORKSPACE
    withEnv(["HOME=${newHome}"]) {
      if (registry && secretRegistry) {
        dockerLogin(secret: "${secretRegistry}", registry: "${registry}")
      }
      retryWithSleep(retries: 2, seconds: 5, backoff: true) {
        sh(label: 'Install precommit', script: "curl -s https://pre-commit.com/install-local.py | python -")
      }
      retryWithSleep(retries: 2, seconds: 5, backoff: true) {
        sh(label: 'Install precommit hooks', script: """
          export PATH=${newHome}/bin:${env.PATH}
          ## Install with the hooks therefore ~/.cache/pre-commit will be created with the repos
          pre-commit install --install-hooks
        """)
      }
      sh(label: 'Run precommit', script: """
        export PATH=${newHome}/bin:${env.PATH}
        ## Search for the repo with the scripts to be added to the PATH
        set +e
        searchFile=\$(find ${newHome}/.cache/pre-commit -type d -name 'scripts' | grep '.ci/scripts')
        if [ -e "\${searchFile}" ] ; then
          export PATH=\${PATH}:\${searchFile}
        else
          echo 'WARN: PATH has not been configured with the shell scripts that might be required!'
        fi
        set -e
        ## Validate the pre-commit for the new changes
        git diff-tree --no-commit-id --name-only -r ${commit} | xargs pre-commit run --files | tee ${reportFileName}
      """)
    }
  }
  if(junitFlag) {
    preCommitToJunit(input: reportFileName, output: "${reportFileName}.xml")
    archiveArtifacts(allowEmptyArchive: true, artifacts: "${reportFileName}.xml")
    junit testResults: "${reportFileName}.xml", allowEmptyResults: true, keepLongStdio: true
  }
}
