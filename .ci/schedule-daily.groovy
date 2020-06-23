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
  agent { label 'master' }
  environment {
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL='INFO'
    DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
    DOCKERELASTIC_SECRET = 'secret/apm-team/ci/docker-registry/prod'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    cron('H H(4-5) * * 1-5')
  }
  stages {
    stage('Run Tasks'){
      steps {
        build(job: 'apm-shared/apm-test-pipeline',
          parameters: [string(name: 'branch_specifier', value: 'master')],
          propagate: false,
          wait: false
        )

        build(job: 'apm-shared/apm-docker-images-pipeline',
          parameters: [
            string(name: 'registry', value: 'docker.elastic.co'),
            string(name: 'tag_prefix', value: 'observability-ci'),
            string(name: 'secret', value: 'secret/apm-team/ci/docker-registry/prod'),
            booleanParam(name: 'nodejs', value: true),
            booleanParam(name: 'python', value: true),
            booleanParam(name: 'ruby', value: true),
            booleanParam(name: 'weblogic', value: true),
            booleanParam(name: 'oracle_instant_client', value: true),
            booleanParam(name: 'apm_integration_testing', value: true),
            booleanParam(name: 'helm_kubectl', value: true),
            booleanParam(name: 'opbot', value: true),
            string(name: 'branch_specifier', value: 'master')
          ],
          propagate: false,
          wait: false
        )

        build(job: 'apm-shared/apm-docker-opbeans-pipeline',
          parameters: [
            string(name: 'registry', value: 'docker.elastic.co'),
            string(name: 'tag_prefix', value: 'observability-ci'),
            string(name: 'version', value: 'daily'),
            string(name: 'secret', value: "${DOCKERELASTIC_SECRET}"),
            booleanParam(name: 'opbeans', value: true),
            string(name: 'branch_specifier', value: 'master')
          ],
          propagate: false,
          wait: false
        )

        build(job: 'apm-ui/apm-ui-e2e-tests-mbp/master',
          parameters: [
            booleanParam(name: 'FORCE', value: true),
          ],
          propagate: false,
          wait: false
        )
      }
    }
    stage('Third-party license scan'){
      matrix {
        axes {
          axis {
            name 'REPO'
            values (
              'apm-agent-dotnet',
              'apm-agent-go',
              'apm-agent-java',
              'apm-agent-js-core',
              'apm-agent-nodejs-opentracing',
              'apm-agent-nodejs',
              'apm-agent-php',
              'apm-agent-python-benchmarks',
              'apm-agent-python',
              'apm-agent-ruby',
              'apm-agent-rum-js',
              'apm-integration-testing',
              'apm-pipeline-library',
              'apm-server',
              'beats',
              //'code',
              //'ctags-langserver',
              //'ctags',
              'ecs-dotnet',
              'ecs-logging-go-zap',
              'ecs-logging-java',
              'ecs-logging-php',
              'ecs-logging-python',
              //'go-langserver-plugin',
              //'go-langserver',
              'go-lookslike',
              //'gradle-code-manifest-plugin',
              'hey-apm',
              'integrations',
              //'java-langserver',
              //'javascript-typescript-langserver',
              //'node-ctags',
              'observability-test-environments',
              'opbeans-dotnet',
              'opbeans-flask',
              'opbeans-frontend',
              'opbeans-go',
              'opbeans-ruby',
              'package-registry',
              'package-storage',
              //'typescript-language-server'
            )
          }
        }
        stages {
          stage('License Scan'){
            steps {
              build(
                job: 'apm-shared/license-scan-general',
                parameters: [
                  string(name: 'branch_specifier', value: 'master'),
                  string(name: 'repo', value:"${env.REPO}")
                ],
                wait: false,
                quietPeriod: 10
              )
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
