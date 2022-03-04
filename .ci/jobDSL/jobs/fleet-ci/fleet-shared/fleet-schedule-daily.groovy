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

pipelineJob("fleet-shared/fleet-schedule-daily") {
  displayName('Jobs scheduled daily')
  description('Jobs scheduled daily from Monday to Friday.')
  disabled(false)
  quietPeriod(10)
  logRotator {
    numToKeep(10)
    daysToKeep(7)
    artifactNumToKeep(10)
    artifactDaysToKeep(-1)
  }
  parameters {
    stringParam("branch_specifier", "main", "the Git branch specifier to build.")
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
            github("elastic/apm-pipeline-library", "ssh")
            credentials("f6c7695a-671e-4f4f-a331-acdce44ff9ba")
          }
          branch('${branch_specifier}')
          extensions {
            wipeOutWorkspace()
          }
        }
      }
      lightweight(false)
      scriptPath(".ci/fleet-ci-schedule-daily.groovy")
    }
  }
}
