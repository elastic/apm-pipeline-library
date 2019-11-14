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

  As long as the BO UI view doesn't show the downstream URL in the view let's use
  the suggested snippet from https://issues.jenkins-ci.org/browse/JENKINS-38339

  Further details: https://brokenco.de/2017/08/03/overriding-builtin-steps-pipeline.html

  build(job: 'foo', parameters: [string(name: "my param", value: some_value)])
*/
def call(Map params = [:]){
  def job = params.job
  def parameters = params.parameters
  def wait = params.get('wait', true)
  def propagate = params.get('propagate', true)
  def quietPeriod = params.get('quietPeriod', 1)

  def buildInfo
  try {
      buildInfo = steps.build(job: job, parameters: parameters, wait: wait, propagate: propagate, quietPeriod: quietPeriod)
  } catch (Exception e) {
      println getBlueOceanLink(e, job)
      throw e
  }
  println getBlueOceanLink(buildInfo, job)
  return buildInfo
}

def getBlueOceanLink(obj, jobName) {
  def buildNumber

  if(obj instanceof Exception) {
    obj.toString().split(" ").each {
      if(it.contains("#")) {
        buildNumber = it.substring(1)
      }
    }
  } else if(obj instanceof org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper) {
    buildNumber = obj.getNumber()
  } else {
    return "Can not determine Blue Ocean link!!!"
  }

  return "For detailed information see: ${env.JENKINS_URL}/blue/organizations/jenkins//${jobName}/detail/${jobName}/${buildNumber}/pipeline"
}
