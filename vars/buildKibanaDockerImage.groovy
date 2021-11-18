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
  buildKibanaDockerImage(refspec: 'PR/12345', dockerRegistry: hub.docker.com)
*/
def call(Map args = [:]){
  def kibanaRefspec = args?.refspec?.trim() ? args.refspec : 'master'
  def uppercaseKibanaRefspec = kibanaRefspec.toUpperCase()

  def refspec = kibanaRefspec
  if (uppercaseKibanaRefspec.startsWith('PR/')) {
    refspec = uppercaseKibanaRefspec
  }
  log(level: 'DEBUG', text: "Kibana refspec is: ${refspec}")

  def deployName = normalize(refspec)
  def packageJSON = isEmptyString(args.packageJSON) ? 'package.json' : args.packageJSON
  def baseDir = isEmptyString(args.baseDir) ? "${env.BASE_DIR}/build" : args.baseDir
  def credentialsId = isEmptyString(args.credentialsId) ? '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken' : args.credentialsId
  def kibanaDockerTargetTag = isEmptyString(args.targetTag) ? '' : args.targetTag
  def dockerRegistry = isEmptyString(args.dockerRegistry) ? 'docker.elastic.co' : args.dockerRegistry
  def dockerRegistrySecret = isEmptyString(args.dockerRegistrySecret) ? 'secret/observability-team/ci/docker-registry/prod' : args.dockerRegistrySecret
  def dockerImageSource = isEmptyString(args.dockerImageSource) ?  "${dockerRegistry}/kibana/kibana" : args.dockerImageSource
  def dockerCloudImageSource = isEmptyString(args.dockerCloudImageSource) ?  "${dockerRegistry}/kibana-ci/kibana-cloud" : args.dockerCloudImageSource
  def dockerImageTarget = isEmptyString(args.dockerImageTarget) ? "${dockerRegistry}/observability-ci/kibana" : args.dockerImageTarget

  log(level: 'DEBUG', text: "Cloning Kibana repository, refspec ${refspec}, into ${baseDir}")

  checkout([$class: 'GitSCM',
    branches: [[name: "*/${refspec}"]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [
      [$class: 'RelativeTargetDirectory', relativeTargetDir: "${baseDir}"],
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

  def kibanaVersion = ''

  dir("${baseDir}"){
    kibanaDockerTargetTag = isEmptyString(kibanaDockerTargetTag) ? getGitCommitSha() : kibanaDockerTargetTag
    setEnvVar('NODE_VERSION', readFile(file: ".node-version")?.trim())

    kibanaVersion = readJSON(file: packageJSON).version + '-SNAPSHOT'

    retryWithSleep(retries: 3) {
      sh(label: 'Build Docker image', script: libraryResource('scripts/build-kibana-docker-image.sh'))
    }
  }

  dockerLogin(secret: "${dockerRegistrySecret}", registry: "${dockerRegistry}")
  log(level: 'DEBUG', text: "Tagging ${dockerImageSource}:${kibanaVersion} to ${dockerImageTarget}:${kibanaDockerTargetTag} and ${dockerImageTarget}:${deployName}")

  retryWithSleep(retries: 3) {
    def tags = [
      "${kibanaDockerTargetTag}",
      "${deployName}",
      "${kibanaVersion}-${kibanaDockerTargetTag}",
      "${kibanaVersion}-${deployName}"
    ]
    def dockerImages = ["${dockerImageSource}", "${dockerCloudImageSource}"]
    tags.each{ tag ->
      dockerImages.each { dockerImage ->
        def src = "${dockerImage}:${kibanaVersion}"
        def dst = "${dockerImage}:${tag}"
        sh(label: 'Push Docker image', script: """
          docker tag ${src} ${dst}
          docker push ${dst}
        """)
      }
    }
  }

  setEnvVar('DEPLOY_NAME', deployName)
  setEnvVar('KIBANA_DOCKER_TAG', "${kibanaVersion}-${deployName}")
  log(level: 'DEBUG', text: "${dockerImageTarget}:${kibanaDockerTargetTag} and ${dockerImageTarget}:${deployName} were pushed")
}

def isEmptyString(value){
  return value == null || value?.trim() == ""
}

def normalize(value){
  return value?.trim().toLowerCase().replaceAll("[^A-Za-z0-9 ]", "")
}
