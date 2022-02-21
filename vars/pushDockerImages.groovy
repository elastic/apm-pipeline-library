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

Publish docker images in the given docker registry. For such, it
retags the existing docker images and publish them in the given
docker namespace.

It also allows to transform the version and customise private docker
namespaces/registries.

  pushDockerImages(
    secret: "my-secret",
    registry: "my-registry",
    arch: 'amd64',
    version: '8.2.0',
    snapshot: true,
    imageName : 'filebeat',
    variants: [
      '' : 'beats',
      '-ubi8' : 'beats',
      '-cloud' : 'beats-ci',
      '-complete' : 'beats',
    ],
    targetNamespace: 'observability-ci'
  )

*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('publishToCDN: windows is not supported yet.')
  }
  def registry = args.containsKey('registry') ? args.registry : error('pushDockerImages: registry parameter is required')
  def secret = args.containsKey('secret') ? args.secret : error('pushDockerImages: secret parameter is required')
  def targetNamespace = args.containsKey('targetNamespace') ? args.targetNamespace : error('pushDockerImages: targetNamespace parameter is required')
  def version = args.containsKey('version') ? args.version : error('pushDockerImages: version parameter is required')
  def imageName = args.containsKey('imageName') ? args.imageName : error('pushDockerImages: imageName parameter is required')
  def arch = args.get('arch', 'amd64')
  def variants = args.get('variants', [:])
  def snapshot = args.get('snapshot', true)

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
  variants?.each { variant, sourceNamespace ->
    tags.each { tag ->
      // TODO:
      // For backward compatibility let's ensure we tag only for amd64, then E2E can benefit from until
      // they support the versioning with the architecture
      if ("${arch}" == "amd64") {
        doTagAndPush(registry: registry, imageName: imageName, variant: variant, sourceTag: sourceTag, targetTag: "${tag}", sourceNamespace: sourceNamespace, targetNamespace: targetNamespace)
      }
      doTagAndPush(registry: registry, imageName: imageName, variant: variant, sourceTag: sourceTag, targetTag: "${tag}-${arch}", sourceNamespace: sourceNamespace, targetNamespace: targetNamespace)
    }
  }
}

/**
* Tag and push the source docker image. It retries to add resilience.
*
* @param imageName of the docker image
* @param variant name of the variant used to build the docker image name
* @param sourceNamespace namespace to be used as source for the docker tag command
* @param targetNamespace namespace to be used as target for the docker tag command
* @param sourceTag tag to be used as source for the docker tag command, usually under the 'beats' namespace
* @param targetTag tag to be used as target for the docker tag command, usually under the 'observability-ci' namespace
*/
def doTagAndPush(Map args = [:]) {
  def name = args.imageName
  def variant = args.variant
  def sourceTag = args.sourceTag
  def targetTag = args.targetTag
  def sourceNamespace = args.sourceNamespace
  def targetNamespace = args.targetNamespace
  def registry = args.registry

  def sourceName = "${registry}/${sourceNamespace}/${name}${variant}:${sourceTag}"
  def targetName = "${registry}/${targetNamespace}/${name}${variant}:${targetTag}"
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
