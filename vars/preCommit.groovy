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
  def dockerImage = params.get('dockerImage')
  if (dockerImage?.trim()) {
    docker.image(dockerImage).inside("-e PATH=${env.PATH}:${env.WORKSPACE}/bin") {
      withEnv(["HOME=${env.WORKSPACE}/${env.BASE_DIR ?: ''}"]) {
        preCommit(params)
      }
    }
  } else {
    preCommit(params)
  }
}

def preCommit(Map params = [:]) {
  def junitFlag = params.get('junit', true)
  def commit = params.get('commit', env.GIT_BASE_COMMIT)
  def credentialsId = params.get('credentialsId', 'f6c7695a-671e-4f4f-a331-acdce44ff9ba')
  def registry = params.get('registry', 'docker.elastic.co')
  def secretRegistry = params.get('secretRegistry', 'secret/apm-team/ci/docker-registry/prod')
  def dockerImage = params.get('dockerImage')

  if (!commit?.trim()) {
    commit = env.GIT_BASE_COMMIT ?: error('preCommit: git commit to compare with is required.')
  }

  def reportFileName = 'pre-commit.out'

  sshagent([credentialsId]) {

    if (dockerImage?.trim()) {
      log(level: 'INFO', text: 'preCommit: dockerImage does not allow to run docker login')
    } else {
      if (registry && secretRegistry) {
        dockerLogin(secret: "${secretRegistry}", registry: "${registry}")
      }
    }
    sh """
      curl https://pre-commit.com/install-local.py | python -
      git diff-tree --no-commit-id --name-only -r ${commit} | xargs pre-commit run --files | tee ${reportFileName}
    """
  }
  if(junitFlag) {
    preCommitToJunit(input: reportFileName, output: "${reportFileName}.xml")
    junit testResults: "${reportFileName}.xml", allowEmptyResults: true, keepLongStdio: true
  }
}
