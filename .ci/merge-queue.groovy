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

@Library('apm@master') _

pipeline {
  agent none
  environment {
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
    PIPELINE_LOG_LEVEL = 'INFO'
  }
  options {
    timeout(time: 3, unit: 'HOURS')
    buildDiscarder(logRotator(daysToKeepStr: '30', artifactNumToKeepStr: '5'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
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
     printContributedVariables: true,
     printPostContent: false,
     silentResponse: true,
     regexpFilterText: '$ref-$x_github_event',
     regexpFilterExpression: '^(refs/tags/current|refs/heads/master/.+)-comment$'
    )
  }
  stages {
    stage('Process GitHub Events') {
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
        whenTrue(shouldBeTriggered()) {
          // when E label ready-to-merge but no merge-queue-running/merge-queue-success
          // when E approvals.size() > 1
          // when GH checks.find { it.failed } == null
          setEnvVar('MERGE_QUEUE', 'true')
          setEnvVar('GITHUB_PULL_REQUEST', 'TBD')
        }
      }
    }
    stage('Trigger Pull Request') {
      when {
        expression { return env.MERGE_QUEUE?.equals('true') }
      }
      steps {
        notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'started')
        addGitHubLabels(pr: env.GITHUB_PULL_REQUEST, labels: 'merge-queue-running')
        triggerJob(pr: env.GITHUB_PULL_REQUEST)
        addGitHubLabels(pr: env.GITHUB_PULL_REQUEST, labels: 'merge-queue-success')
        unassignGitHubLabels(pr: env.GITHUB_PULL_REQUEST, labels: [ 'merge-queue-running', 'ready-to-merge' ])
      }
      post {
        unsuccessful {
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'failed')
        }
        success {
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'completed')  
        }
      }
    }
    stage('Merge Pull Request') {
      when {
        expression { return env.MERGE_QUEUE?.equals('true') }
      }
      steps {
        merge(pr: env.GITHUB_PULL_REQUEST, type: 'squash-rebase')
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult(prComment: false, slackComment: true, analyzeFlakey: false)
    }
  }
}

def shouldBeTriggered() {
  return env.GITHUB_COMMENT?.contains('add to merge-queue') &&
         env.GITHUB_LABEL?.contains('ready-to-merge') &&
         env.GITHUB_CHECKS?.contains('true') &&
         env.GITHUB_APPROVALS?.contains('passed')
}
