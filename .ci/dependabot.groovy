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

@Library('apm@current') _

pipeline {
  agent none
  environment {
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    PIPELINE_LOG_LEVEL='INFO'
  }
  options {
    timeout(time: 5, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  stages {
    stage('Run dependabot'){
      matrix {
        agent any
        axes {
          axis {
            name 'PROJECT'
            values 'apm-agent-dotnet', 'apm-agent-go', 'apm-agent-java', 'apm-agent-nodejs', 'apm-agent-python', 'apm-agent-ruby', 'apm-agent-rum-js', 'apm-server', 'opbeans-dotnet', 'opbeans-go', 'opbeans-java', 'opbeans-frontend', 'opbeans-node', 'opbeans-python', 'opbeans-ruby', 'apm-pipeline-library'
          }
        }
        stages {
          stage('Dependabot') {
            environment {
              ORG_REPO = "elastic/${PROJECT}"
              CONFIGURATION_FILE = ".ci/dependabot.yml"
            }
            steps {
              git "https://github.com/${ORG_REPO}.git"
              script {
                if (fileExists("${CONFIGURATION_FILE}")) {
                  def config = readYaml(file: "${CONFIGURATION_FILE}")
                  def assign = config.assign
                  def manager = config.manager
                  manager.split(',').each { packageManager ->
                    dependabot(project: "${ORG_REPO}", assign: assign, package: packageManager)
                  }
                } else {
                  echo "Dependabot not enabled for the project: ${ORG_REPO}."
                }
              }
            }
          }
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
