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

Publish the give docker images in the given docker registry. For such, it
retags the existing docker images and publish them in the given
docker namespace.

  pushDockerImages(
    registry: "my-registry",
    secret: "my-secret",
    version: '8.2.0',
    snapshot: true,
    images: [
      [ source: "beats/filebeat", arch: 'amd64', target: "observability-ci/filebeat"],
      [ source: "beats/filebeat-ubi8", arch: 'amd64', target: "observability-ci/filebeat-ubi8"],
      [ source: "beats-ci/filebeat-cloud", arch: 'amd64', target: "observability-ci/filebeat-cloud"],
      [ source: "beats-ci/filebeat-complete", arch: 'amd64', target: "observability-ci/filebeat-complete"]
    ]
  )

*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('pushDockerImages: windows is not supported yet.')
  }
  def registry = args.containsKey('registry') ? args.registry : error('pushDockerImages: registry parameter is required')
  def secret = args.containsKey('secret') ? args.secret : error('pushDockerImages: secret parameter is required')
  def version = args.containsKey('version') ? args.version : error('pushDockerImages: version parameter is required')
  def snapshot = args.get('snapshot', true)
  def images = args.get('images', [:])

  // Transform version in a snapshot.
  def sourceTag = version
  def aliasVersion = ""
  if (snapshot) {
    // remove third number in version
    aliasVersion = version.substring(0, version.lastIndexOf(".")) + "-SNAPSHOT"
    sourceTag += "-SNAPSHOT"
  }

  // What docker tags are gonna be used
  def tags = calculateTags(sourceTag, aliasVersion)

  dockerLogin(secret: "${secret}", registry: "${registry}")
  images?.each { image ->
    tags.each { tag ->
      // TODO:
      // For backward compatibility let's ensure we tag only for amd64, then E2E can benefit from until
      // they support the versioning with the architecture
      if ("${image.arch}" == "amd64") {
        doTagAndPush(registry: registry, sourceTag: sourceTag, targetTag: "${tag}", source: image.source, target: image.target)
      }
      doTagAndPush(registry: registry, sourceTag: sourceTag, targetTag: "${tag}-${image.arch}", source: image.source, target: image.target)
    }
  }
}

/**
* Tag and push the source docker image. It retries to add resilience.
*
* @param source the namespace and docker image to be used
* @param target the namespace and docker image to be pushed
* @param sourceTag tag to be used as source for the docker tag command, usually under the 'beats' namespace
* @param targetTag tag to be used as target for the docker tag command, usually under the 'observability-ci' namespace
* @param registry the docker registry
*/
def doTagAndPush(Map args = [:]) {
  def registry = args.registry
  def source = args.source
  def sourceTag = args.sourceTag
  def target = args.target
  def targetTag = args.targetTag

  def sourceName = "${registry}/${source}:${sourceTag}"
  def targetName = "${registry}/${target}:${targetTag}"
  def iterations = 0

  waitUntil(initialRecurrencePeriod: 5000) {
    iterations++
    def status = sh(label: "Change tag and push ${targetName}",
                    script: """#!/bin/bash
                      set -e
                      echo "source: '${sourceName}' target: '${targetName}'"
                      if docker image inspect "${sourceName}" &> /dev/null ; then
                        docker tag "${sourceName}" "${targetName}"
                        docker push "${targetName}"
                      else
                        echo "docker image ${sourceName} does not exist"
                      fi""",
                    returnStatus: true)
    // exit if above command run successfully or it reached the max of iterations.
    return (status == 0 || iterations >= 3)
  }
}

def calculateTags(sourceTag, aliasVersion) {
  def tags = [ env.GIT_BASE_COMMIT,
               isPR() ? "pr-${env.CHANGE_ID}" : sourceTag ]

  if (!isPR() && aliasVersion.trim()) {
    tags << aliasVersion
  }
  return tags
}
