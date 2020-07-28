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
  def project = args.project
  def content = args.content
  
  def mapOfStages = [:]

  def defaultNode = content.containsKey('platform') ? content.platform : ''

  content?.stages?.each { name, value ->
    if (value.containsKey('platforms')) {
      value.platforms.each { platform ->
        def stageName = "${project}-${name}-${platform}"
        log(level: 'DEBUG', text: "stage: ${stageName}")
        mapOfStages[stageName] = generateStage(label: platform, content: value)
      }
    } else {
      def stageName = "${project}-${name}"
      log(level: 'DEBUG', text: "stage: ${stageName}")
      mapOfStages["${stageName}"] = generateStage(label: defaultNode, content: value)
    }
  }

  return mapOfStages
}

private generateStage(Map args = [:]) {
  def content = args.content
  def label = args.label
  return {
    // TODO TBDnode(label) {
      echo "${content.command} in ${label}"
    //}
  }
}
