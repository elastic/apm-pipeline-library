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

// NOTE: Consumers should use the current tag
// @Library('apm@current') _
// NOTE: Master branch will contain the upcoming release changes
//       this will help us to detect any breaking changes in production.
@Library('apm@master') _

pipeline {

  agent { label 'linux && immutable' }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
    LANG = "C.UTF-8"
    LC_ALL = "C.UTF-8"
    PYTHONUTF8 = "1"
  }
  stages {
    stage('Checkout') {
      options { skipDefaultCheckout() }
      steps {
        pipelineManager([apmTraces : [ when: 'ALWAYS' ] ])
        sleep 10
        deleteDir()
        apmCli(spanCommand: "sleep ${randomNumber(min: 1, max: 30)}s")
      }
    }
    stage('lint') {
      options { skipDefaultCheckout() }
      steps {
        deleteDir()
        apmCli(spanCommand: "sleep ${randomNumber(min: 1, max: 30)}s")
      }
    }
    stage('test') {
      options { skipDefaultCheckout() }
      steps {
        deleteDir()
        apmCli(spanCommand: "sleep ${randomNumber(min: 1, max: 30)}s")
      }
    }
    stage('build Fail') {
      options { skipDefaultCheckout() }
      steps {
        withAPM(){
          deleteDir()
          sh("exit 1")
        }
      }
    }
  }
}
