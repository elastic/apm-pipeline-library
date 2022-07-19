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
    disableConcurrentBuilds()
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
              value('$.repository.full_name')
            }
            genericVariable {
              key("GT_REF")
              value('$.ref')
            }
            genericVariable {
              key("GT_BEFORE")
              value('$.before')
            }
            genericVariable {
              key("GT_AFTER")
              value('$.after')
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
            genericVariable {
              key("GT_TITLE")
              value('$.pull_request.title')
            }
            genericVariable {
              key("GT_PR_HEAD_REF")
              value('$.pull_request.head.ref')
            }
            genericVariable {
              key("GT_PR_HEAD_SHA")
              value('$.pull_request.head.sha')
            }
            genericVariable {
              key("GT_ACTION")
              value('$.action')
            }
          }
          genericHeaderVariables {
            genericHeaderVariable {
              key("x-github-event")
              regexpFilter("pull_request")
            }
          }
          regexpFilterText('$GT_REPO/$GT_PR_HEAD_REF/$GT_TITLE/$GT_ACTION')
          regexpFilterExpression("^elastic/observability-test-environments/updatecli_.*/\\[updatecli\\]\\[edge-lite\\] Update Elastic Stack/opened")
          causeString("Triggered on create PR")
          silentResponse(true)
        }
      }
    }
  }
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
