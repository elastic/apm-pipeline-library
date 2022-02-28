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

  buildKibanaDockerImage(refspec: 'main')
  buildKibanaDockerImage(refspec: 'PR/12345')
  buildKibanaDockerImage(refspec: 'PR/12345', dockerRegistry: hub.docker.com)
*/
def call(Map args = [:]){
  def kibanaRefspec = args?.refspec?.trim() ? args.refspec : 'main'
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
  def dockerCloudImageTarget = isEmptyString(args.dockerImageTarget) ? "${dockerRegistry}/observability-ci/kibana-cloud" : args.dockerImageTarget
  def referenceRepo = args.reference ?: "/var/lib/jenkins/kibana.git"
  def depth  = args.depth as Integer ?: 1
  def shallow = args.shallow ?: true
  def skipUbuntu = args.skipUbuntu ?: false
  def skipCloud = args.skipCloud ?: false

  log(level: 'DEBUG', text: "Cloning Kibana repository, refspec ${refspec}, into ${baseDir}")

  fastCheckout(baseDir: baseDir, reference: referenceRepo, credentialsId: credentialsId, depth: depth, shallow: true, url: 'http://github.com/elastic/kibana.git', refspec: refspec)

  def kibanaVersion = ''

  dir("${baseDir}"){
    kibanaDockerTargetTag = isEmptyString(kibanaDockerTargetTag) ? getGitCommitSha() : kibanaDockerTargetTag
    setEnvVar('NODE_VERSION', readFile(file: ".node-version")?.trim())
    setEnvVar('BUILD_DOCKER_UBUNTU', skipUbuntu ? '' : '1')
    setEnvVar('BUILD_DOCKER_CLOUD', skipCloud ? '' : '1')

    kibanaVersion = readJSON(file: packageJSON).version + '-SNAPSHOT'

    retryWithSleep(retries: 3) {
      sh(label: 'Build Docker image', script: libraryResource('scripts/build-kibana-docker-image.sh'))
    }
  }

  dockerLogin(secret: "${dockerRegistrySecret}", registry: "${dockerRegistry}")

  retryWithSleep(retries: 3) {
    def tags = [
      "${kibanaDockerTargetTag}",
      "${deployName}",
      "${kibanaVersion}-${kibanaDockerTargetTag}",
      "${kibanaVersion}-${deployName}"
    ]
    def dockerImages = []

    if(skipUbuntu == false){
      dockerImages.add([src:"${dockerImageSource}", dst:"${dockerImageTarget}"])
    }
    if(skipCloud == false){
      dockerImages.add([src:"${dockerCloudImageSource}", dst:"${dockerCloudImageTarget}"])
    }
    log(level: 'DEBUG', text: dockerImages.toString())


    tags.each{ tag ->
      dockerImages.each { dockerImage ->
        def src = "${dockerImage.src}:${kibanaVersion}"
        def dst = "${dockerImage.dst}:${tag}"
        sh(label: 'Push Docker image', script: """
          docker tag ${src} ${dst}
          docker push ${dst}
        """)
        log(level: 'DEBUG', text: "Tagging ${src} to ${dst}")
        log(level: 'DEBUG', text: "${dst} was pushed")
      }
    }
  }

  setEnvVar('DEPLOY_NAME', deployName)
  setEnvVar('KIBANA_DOCKER_TAG', "${kibanaVersion}-${deployName}")
}

def isEmptyString(value){
  return value == null || value?.trim() == ""
}

def normalize(value){
  return value?.trim().toLowerCase().replaceAll("[^A-Za-z0-9 ]", "")
}
