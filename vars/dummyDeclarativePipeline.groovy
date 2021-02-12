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
 A sample of a step implementation as a declarative pipeline.

 dummyDeclarativePipeline()

*/
def call(Map args = [:]) {
  pipeline {
    agent { label 'linux && immutable' }
    environment {
      BASE_DIR = 'src/github.com/elastic'
      // ? required for the testing.
      FOO = "${args?.FOO}"
    }
    options {
      timeout(time: 1, unit: 'HOURS')
    }
    parameters {
      string(name: 'FOO', defaultValue: '', description: 'FOO param')
    }
    stages {
      stage('Checkout') {
        options {
          skipDefaultCheckout()
        }
        steps {
          deleteDir()
          gitCheckout(basedir: "${BASE_DIR}")
          stash allowEmpty: true, name: 'source', useDefaultExcludes: false
        }
      }
      /**
      Build the project from code..
      */
      stage('Build') {
        options {
          skipDefaultCheckout()
        }
        steps {
          deleteDir()
          unstash 'source'
          dir("${env.BASE_DIR}"){
            echo "FOO='${env.FOO}'"
          }
        }
      }
    }
    post {
      cleanup {
        notifyBuildResult()
      }
    }
  }
}
