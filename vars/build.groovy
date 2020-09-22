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

import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper
import hudson.model.Result
import co.elastic.BuildException

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
  def url
  try {
    buildInfo = steps.build(job: job, parameters: parameters, wait: wait, propagate: false, quietPeriod: quietPeriod)
    url = getRedirectLink(buildInfo, job)
  } catch (Exception e) {
    def buildLogOutput = currentBuild.rawBuild.getLog(2).find { it.contains('Starting building') }
    url = getRedirectLink(buildLogOutput, job)
    throw new BuildException(getBuildId(buildLogOutput), Result.FAILURE, e.getCauses())
  } finally {
    log(level: 'INFO', text: "${url}")
  }

  // Propagate the build error if required
  if (propagate) {
    propagateFailure(buildInfo)
  }
  return buildInfo
}

def getBuildId(buildInfo) {
  def buildNumber = ''
  if(buildInfo instanceof String) {
    buildInfo.toString().split(' ').each {
      if(it.contains('#')) {
        buildNumber = it.substring(1)
      }
    }
  } else if(buildInfo instanceof RunWrapper) {
    buildNumber = buildInfo.getNumber()
  }
  return buildNumber
}

def getRedirectLink(buildInfo, jobName) {
  if(buildInfo instanceof String) {
    def buildNumber = getBuildId(buildInfo)
    if (buildNumber.trim()) {
      return "For detailed information see: ${env.JENKINS_URL}job/${jobName.replaceAll('/', '/job/')}/${buildNumber}/display/redirect"
    } else {
      return "Can not determine redirect link!!!"
    }
  } else if(buildInfo instanceof RunWrapper) {
    return "For detailed information see: ${buildInfo.getAbsoluteUrl()}display/redirect"
  } else {
    return "Can not determine redirect link!!!"
  }
}

def throwBuildException(buildInfo) {
  log(level: 'DEBUG', text: "${buildInfo.getProjectName()}#${buildInfo.getNumber()} with issue '${buildInfo.getDescription()?.trim() ?: ''}'")
  throw new BuildException(String.valueOf(buildInfo.getNumber()), Result.FAILURE)
}

def propagateFailure(buildInfo) {
  if (buildInfo) {
    if (buildInfo.resultIsWorseOrEqualTo('FAILURE')) {
      throwBuildException(buildInfo)
    }
  } else {
    log(level: 'DEBUG', text: 'buildInfo is not an object.')
  }
}
