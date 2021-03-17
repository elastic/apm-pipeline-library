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

  Builds the Docker image for Kibana, from a branch or a pull Request.

  buildKibanaDockerImage(refspec: 'master')
  buildKibanaDockerImage(refspec: 'PR/12345')
*/
def call(Map args = [:]){
  def refspec = args?.refspec?.trim() ? args.refspec : 'master'
  def uppercaseRefspec = refspec.toUpperCase()

  def kibanaRefspec = refspec
  if (uppercaseRefspec.startsWith('PR/')) {
    kibanaRefspec = uppercaseRefspec
  }
  log(level: 'INFO', text: "Kibana refspec is: ${kibanaRefspec}")

  //buildKibana(refspec: "${kibanaRefspec}")
}

def buildKibana(Map args = [:]) {
  def refspec = args.refspec
  def deployName = normalize(refspec)
  def credentialsId = !isEmptyString(args.credentialsId) ? args.credentialsId : '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'
  def kibanaDockerTargetTag = !isEmptyString(args.targetTag) ? args.targetTag : getGitCommitSha()
  def dockerRegistry = !isEmptyString(args.dockerRegistry) ? args.dockerRegistry : 'docker.elastic.co'
  def dockerRegistrySecret = !isEmptyString(args.dockerRegistrySecret) ? args.dockerRegistrySecret : 'secret/observability-team/ci/docker-registry/prod'
  def dockerImageSource = !isEmptyString(args.dockerImageSource) ? args.dockerImageSource : "${dockerRegistry}/kibana/kibana"
  def dockerImageTarget = !isEmptyString(args.dockerImageTarget) ? args.dockerImageTarget : "${dockerRegistry}/observability-ci/kibana"

  log(level: 'INFO', text: "Cloning Kibana repository, refspec ${refspec}")

  checkout([$class: 'GitSCM',
    branches: [[name: "*/${refspec}"]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [
      [$class: 'RelativeTargetDirectory', relativeTargetDir: "${env.BASE_DIR}"],
      [$class: 'CheckoutOption', timeout: 15],
      [$class: 'AuthorInChangelog'],
      [$class: 'IgnoreNotifyCommit'],
      [$class: 'CloneOption',
        depth: 0,
        noTags: false,
        reference: "/var/lib/jenkins/kibana.git",
        shallow: true,
        timeout: 15
      ]],
      submoduleCfg: [],
      userRemoteConfigs: [[
        credentialsId: "${credentialsId}",
        url: "http://github.com/elastic/kibana.git",
        refspec: '+refs/heads/*:refs/remotes/origin/* +refs/pull/*/head:refs/remotes/origin/PR/*',
        ]]
  ])

  dir("${env.BASE_DIR}"){
    setEnvVar('NODE_VERSION', readFile(file: ".node-version")?.trim())
    setEnvVar('KIBANA_DOCKER_TAG', readJSON(file: 'package.json').version + '-SNAPSHOT')

    retryWithSleep(retries: 3) {
      sh(label: 'Build Docker image', script: libraryResource('scripts/build-kibana-docker-image.sh'))
    }
  }

  dockerLogin(secret: "${dockerRegistrySecret}", registry: "${dockerRegistry}")
  retryWithSleep(retries: 3) {
    sh(label: 'Push Docker image', script: """
      docker tag ${dockerImageSource}:${KIBANA_DOCKER_TAG} ${dockerImageTarget}:${kibanaDockerTargetTag}
      docker tag ${dockerImageSource}:${KIBANA_DOCKER_TAG} ${dockerImageTarget}:${deployName}
      docker push ${dockerImageTarget}:${kibanaDockerTargetTag}
      docker push ${dockerImageTarget}:${deployName}
    """)
  }
}

def isEmptyString(value){
  return value == null || value?.trim() == ""
}

def normalize(value){
  return value?.trim().toLowerCase().replaceAll("[^A-Za-z0-9 ]", "")
}
