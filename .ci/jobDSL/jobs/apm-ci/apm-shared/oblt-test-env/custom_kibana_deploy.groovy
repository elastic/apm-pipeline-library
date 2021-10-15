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

pipelineJob("apm-shared/oblt-test-env/custom-kibana-deploy") {
  displayName('Custom Kibana - Deploy')
  description('Job to deploy Custom Kibana deployments')
  parameters {
    stringParam("branch_specifier", "master", "the Git branch specifier to build.")
    stringParam('environment', "edge", "Test environment branch name to make the deploy into.")
    stringParam('stack_version', "", "Force to use an stack version for Helm chart and other Elastic stack configuration related.")
    stringParam('kibana_branch', "master", "Branch/Tag/pr/commit to use to build the Docker image. (e.g PR/10000)")
    stringParam('target_tag', "", "Tag used fo the generated Docker image.")
    stringParam('slack_channel', "#observablt-bots", "Slack channel to notify the results.")
    booleanParam('build_kibana', true, 'Allow to skip the Kibana build stages.')
    booleanParam('deploy_kibana', true, 'Allow to skip the Kibana deploy stage.')
  }
  disabled(false)
  quietPeriod(10)
  logRotator {
    numToKeep(10)
    daysToKeep(7)
    artifactNumToKeep(10)
    artifactDaysToKeep(-1)
  }
  properties {
    pipelineTriggers {
        triggers {
          issueCommentTrigger{
            commentPattern('(?i)^\\/oblt-deploy$')
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
      scriptPath(".ci/customKibana.groovy")
    }
  }
}
