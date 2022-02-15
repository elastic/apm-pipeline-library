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
  https://plugins.jenkins.io/github
  Update the commit status on GitHub with the current status of the build.

  updateGithubCommitStatus(
    repoUrl: "${GIT_URL}",
    commitSha: "${GIT_COMMIT}",
    message: 'Build result.'
  )

  updateGithubCommitStatus()

  updateGithubCommitStatus(message: 'Build result.')
*/
def call(Map args = [:]) {

}

/**
* @param arch what architecture
*/
def pushDockerImages(Map args = [:]) {
  def name = args.containsKey('name') ? args.name : error('foo')
  def arch = args.get('arch', 'amd64')
  def variants = args.get('variants', [''])
  dockerLogin(secret: "${env.DOCKER_ELASTIC_SECRET}", registry: "${env.DOCKER_REGISTRY}")
  def libbetaVer = env.BEAT_VERSION
  def aliasVersion = ""
  if("${env.SNAPSHOT}" == "true"){
    aliasVersion = libbetaVer.substring(0, libbetaVer.lastIndexOf(".")) // remove third number in version

    libbetaVer += "-SNAPSHOT"
    aliasVersion += "-SNAPSHOT"
  }

  def tagName = "${libbetaVer}"
  if (isPR()) {
    tagName = "pr-${env.CHANGE_ID}"
  }

  // supported tags
  def tags = [tagName, "${env.GIT_BASE_COMMIT}"]
  if (!isPR() && aliasVersion != "") {
    tags << aliasVersion
  }

  variants.each { variant ->
    // cloud docker images are stored in the private docker namespace.
    def sourceNamespace = variant.equals('-cloud') ? 'beats-ci' : 'beats'
    tags.each { tag ->
      // TODO:
      // For backward compatibility let's ensure we tag only for amd64, then E2E can benefit from until
      // they support the versioning with the architecture
      if ("${arch}" == "amd64") {
        doTagAndPush(name: name, variant: variant, sourceTag: libbetaVer, targetTag: "${tag}", sourceNamespace: sourceNamespace)
      }
      doTagAndPush(name: name, variant: variant, sourceTag: libbetaVer, targetTag: "${tag}-${arch}", sourceNamespace: sourceNamespace)
    }
  }
}

/**
* @param name of the docker image
* @param variant name of the variant used to build the docker image name
* @param sourceNamespace namespace to be used as source for the docker tag command
* @param targetNamespace namespace to be used as target for the docker tag command
* @param sourceTag tag to be used as source for the docker tag command, usually under the 'beats' namespace
* @param targetTag tag to be used as target for the docker tag command, usually under the 'observability-ci' namespace
*/
def doTagAndPush(Map args = [:]) {
  def name = args.name
  def variant = args.variant
  def sourceTag = args.sourceTag
  def targetTag = args.targetTag
  def sourceNamespace = args.sourceNamespace
  def targetNamespace = args.targetNamespace
  def sourceName = "${env.DOCKER_REGISTRY}/${sourceNamespace}/${name}${variant}:${sourceTag}"
  def targetName = "${env.DOCKER_REGISTRY}/${targetNamespace}/${name}${variant}:${targetTag}"
  def iterations = 0
  retryWithSleep(retries: 3, seconds: 5, backoff: true) {
    iterations++
    def status = sh(label: "Change tag and push ${targetName}",
                    script: ".ci/scripts/docker-tag-push.sh ${sourceName} ${targetName}",
                    returnStatus: true)
    if (status > 0 && iterations < 3) {
      error("tag and push failed for ${name}, retry")
    } else if (status > 0) {
      log(level: 'WARN', text: "${name} doesn't have ${variant} docker images. See https://github.com/elastic/beats/pull/21621")
    }
  }
}
