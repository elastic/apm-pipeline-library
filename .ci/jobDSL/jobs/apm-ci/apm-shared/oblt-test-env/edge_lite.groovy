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

pipelineJob("apm-shared/oblt-test-env/edge-lite-oblt-cluster") {
  displayName("edge-lite-oblt cluster")
  description("Job to create a edge-lite-oblt cluster every day.")
  parameters {
    stringParam('branch_specifier', "main", "the Git branch specifier to build.")
    booleanParam('notify', true, 'Send notifications about the build result.')
  }
  disabled(false)
  quietPeriod(10)
  properties {
    buildDiscarder {
      strategy {
        logRotator {
          numToKeepStr("10")
          daysToKeepStr("7")
          artifactNumToKeepStr("10")
          artifactDaysToKeepStr("-1")
        }
      }
    }
    disableConcurrentBuilds{
      abortPrevious(false)
    }
    durabilityHint {
      hint("PERFORMANCE_OPTIMIZED")
    }
    disableResume()
    pipelineTriggers {
      triggers {
        GenericTrigger {
          genericVariables {
            genericVariable {
              key("GT_REPO")
              value("\$.repository.full_name")
            }
            genericVariable {
              key("GT_REF")
              value("\$.ref")
            }
            genericVariable {
              key("GT_BEFORE")
              value("\$.before")
            }
            genericVariable {
              key("GT_AFTER")
              value("\$.after")
            }
            genericVariable {
              key("GT_FILES_ADDED")
              value("\$.commits[*].['added'][*]")
            }
            genericVariable {
              key("GT_FILES_MODIFIED")
              value("\$.commits[*].['modified'][*]")
            }
            genericVariable {
              key("GT_FILES_REMOVED")
              value("\$.commits[*].['removed'][*]")
            }
          }
          genericHeaderVariables {
            genericHeaderVariable {
              key("x-github-event")
              regexpFilter("push")
            }
          }
          regexpFilterText("$GT_REPO/$GT_REF$GT_FILES_ADDED$GT_FILES_MODIFIED$GT_FILES_REMOVED")
          regexpFilterExpression("^elastic/observability-test-environments/refs/heads/main.*environments/edge-lite/.*")
          causeString("Triggered on Update PR")
          silentResponse(true)
        }
      }
    }
  }

  /*
  triggers {
    GenericTrigger(
     genericVariables: [
      [key: 'GT_REPO', value: '$.repository.full_name'],
      [key: 'GT_REF', value: '$.ref'],
      [key: 'GT_BEFORE', value: '$.before'],
      [key: 'GT_AFTER', value: '$.after'],
      [key: 'GT_FILES_ADDED', value: "\$.commits[*].['added'][*]"],
      [key: 'GT_FILES_MODIFIED', value: "\$.commits[*].['modified'][*]"],
      [key: 'GT_FILES_REMOVED', value: "\$.commits[*].['removed'][*]"],
    ],
    genericHeaderVariables: [
     [key: 'x-github-event', regexpFilter: 'push']
    ],
     causeString: 'Triggered on push',
     printContributedVariables: false,
     printPostContent: false,
     silentResponse: true,
     regexpFilterText: '$GT_REPO/$GT_REF$GT_FILES_ADDED$GT_FILES_MODIFIED$GT_FILES_REMOVED',
     regexpFilterExpression: '^elastic/observability-test-environments/refs/heads/main.*environments/users/.*'
    )
  }
  */
  definition {
    cpsScm {
      scm {
        git {
          remote {
            github("elastic/observability-test-environments", "ssh")
            credentials("f6c7695a-671e-4f4f-a331-acdce44ff9ba")
          }
          branch('${branch_specifier}')
          extensions {
            wipeOutWorkspace()
          }
        }
      }
      lightweight(false)
      scriptPath(".ci/edge-lite.groovy")
    }
  }
}
