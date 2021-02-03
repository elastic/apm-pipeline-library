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
        [key: 'action', value: '$.action'],         // This is the event
        [key: 'label', value: '$.label.name'],      // Label name
        [key: 'review', value: '$.review.state'],   // Review status
        [key: 'comment_id', value: '$.comment.id'], // Comment
        [key: 'ref', value: '$.ref'],
        [key: 'repo', value: '$.repository.name'],
        [key: 'payload', value: '$'],
      ],
      causeString: 'Triggered on $ref',
      printContributedVariables: true,
      printPostContent: false,
      silentResponse: false
    )
  }
  stages {
    stage('Process GitHub Events') {
      steps {
        whenTrue(shouldBeTriggered()) {
          // when E label ready-to-merge but no merge-queue-running/merge-queue-success
          // when E approvals.size() > 1
          // when GH checks.find { it.failed } == null
          setEnvVar('MERGE_QUEUE', 'true')
          setEnvVar('GITHUB_PULL_REQUEST', 'TBD')
        }
      }
      post {
        unsuccessful {
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'critical', phase: 'events')
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
      }
      post {
        unsuccessful {
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'failed', phase: 'trigger')
        }
        success {
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'completed', phase: 'trigger')
        }
      }
    }
    stage('Change GitHub labels') {
      when {
        expression { return env.MERGE_QUEUE?.equals('true') }
      }
      steps {
        addGitHubLabels(pr: env.GITHUB_PULL_REQUEST, labels: 'merge-queue-success')
        unassignGitHubLabels(pr: env.GITHUB_PULL_REQUEST, labels: [ 'merge-queue-running', 'ready-to-merge' ])
      }
      post {
        unsuccessful {
          // notify to the merge-queue owner that the automation failed.
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'critical', phase: 'labels')
        }
      }
    }
    stage('Merge Pull Request') {
      when {
        expression { return env.MERGE_QUEUE?.equals('true') }
      }
      steps {
        notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'failed')
        merge(pr: env.GITHUB_PULL_REQUEST, type: 'squash-rebase')
      }
      post {
        unsuccessful {
          // notify to the merge-queue owner that the automation failed.
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'critical', phase: 'merge')
        }
        success {
          notifyMergeQueue(pr: env.GITHUB_PULL_REQUEST, status: 'completed', phase: 'merge')
        }
      }
    }
  }
}

def shouldBeTriggered() {
  return env.GITHUB_COMMENT?.contains('add to merge-queue') &&
         env.GITHUB_LABEL?.contains('ready-to-merge') &&
         env.GITHUB_CHECKS?.contains('true') &&
         env.GITHUB_APPROVALS?.contains('passed')
}

def notifyMergeQueue(Map args = [:]) {
    // if critical then reports in slack to the system admin!
    // report also as a GitHub comment.
}
