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
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
    PIPELINE_LOG_LEVEL = 'INFO'
    BRANCH_NAME = "${params.branch_specifier}"
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  //http://JENKINS_URL/generic-webhook-trigger/invoke
  triggers {
    GenericTrigger(
     genericVariables: [
      [key: 'ref', value: '$.ref'],
      [key: 'repo', value: '$.repository.name'],
      [key: 'after', value: '$.after'],
      [key: 'payload', value: '$'],
      [key: 'comment_id', value: '$.comment.id'],
      [key: 'payload', value: '$'],
     ],
    genericHeaderVariables: [
     [key: 'x-github-event', regexpFilter: '']
    ],
     causeString: 'Triggered on $ref',
     //Allow to use a credential as a secret to trigger the webhook
     //tokenCredentialId: '',
     printContributedVariables: true,
     printPostContent: false,
     silentResponse: true,
     regexpFilterText: '$ref-$x_github_event',
     regexpFilterExpression: '^(refs/tags/current|refs/heads/master/.+)-comment$'
    )
  }
  parameters {
    string(name: 'branch_specifier', defaultValue: "master", description: "the Git branch specifier to build")
  }
  stages {
    stage('Process GitHub Event') {
      steps {
          echo """
          ref:${env.ref}
          repo:${repo}
          after:${env.after}
          x_github_event:${env.x_github_event}
          comment_id:${env.comment_id}
          """
          whenTrue("payload" == "${env.payload}"){
            echo """
            payload_action:${payload_action}
            payload_repository_name:${payload_repository_name}
            """
          }
          whenTrue("issue_comment" == "${env.x_github_event}"){
            echo """
            payload_comment_id:${payload_comment_id}
            payload_comment_body:${payload_comment_body}
            payload_issue_number:${payload_issue_number}
            """
            setEnvVar('GITHUB_COMMENT', payload_comment_body.split('\n')[0])
          }
      }
    }
  }
}
