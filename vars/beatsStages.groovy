#!/usr/bin/env groovy
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
* Given the YAML definition then it creates all the stages
*/
Map call(Map args = [:]){
  def project = args.containsKey('project') ? args.project : error('beatsStages: project param is required')
  def content = args.containsKey('content') ? args.content : error('beatsStages: content param is required')
  def defaultNode = content.containsKey('platform') ? content.platform : error('beatsStages: platform entry in the content is required.')

  def mapOfStages = [:]

  content?.stages?.each { stageName, value ->
    def tempMapOfStages = [:]
    if (value.containsKey('when')) {
      if (beatsWhen(project: project, content: value.when)) {
        tempMapOfStages = generateStages(content: value, project: project, stageName: stageName, defaultNode: defaultNode)
      }
    } else {
      tempMapOfStages = generateStages(content: value, project: project, stageName: stageName, defaultNode: defaultNode)
    }
    tempMapOfStages.each { k,v -> mapOfStages["${k}"] = v }
  }

  return mapOfStages
}

private generateStages(Map args = [:]) {
  def content = args.content
  def project = args.project
  def stageName = args.stageName
  def defaultNode = args.defaultNode

  def mapOfStages = [:]
  if (content.containsKey('platforms')) {
    content.platforms.each { platform ->
      def id = "${project}-${stageName}-${platform}"
      log(level: 'DEBUG', text: "stage: ${id}")
      mapOfStages[id] = generateStage(label: platform, content: content)
    }
  } else {
    def id = "${project}-${stageName}"
    log(level: 'DEBUG', text: "stage: ${id}")
    mapOfStages["${id}"] = generateStage(label: defaultNode, content: content)
  }
  return mapOfStages
}

private generateStage(Map args = [:]) {
  def content = args.content
  def label = args.label

  def command = content?.containsKey('command') ? content.command : error('beatsStages: command entry in the stage is required.')
  return {
    // TODO TBDnode(label) {
      echo "${content.command} in ${label}"
    //}
  }
}
