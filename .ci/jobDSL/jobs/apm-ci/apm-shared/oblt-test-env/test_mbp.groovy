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

multibranchPipelineJob('apm-shared/oblt-test-env/test-mbp') {
  primaryView('All')
  displayName('Job apm-shared/oblt-test-env/oblt-test-env-custom-kibana')
  description('Job apm-shared/oblt-test-env/oblt-test-env-custom-kibana description')
  orphanedItemStrategy {
    discardOldItems {
      numToKeep(20)
      daysToKeep(7)
    }
  }
  branchSources {
    branchSource {
      source {
        github {
          id('apm-shared/oblt-test-env/oblt-test-env-custom-kibana') // IMPORTANT: use a constant and unique identifier
          credentialsId('2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
          repoOwner('elastic')
          repository('apm-pipeline-library')
          repositoryUrl('https://github.com/elastic/apm-pipeline-library.git')
          configuredByUrl(true)
          // The behaviours control what is discovered from the GitHub repository.
          traits {
            checkoutOptionTrait {
              extension {
                // Specify a timeout (in minutes) for checkout.
                timeout(15)
              }
            }
            cleanBeforeCheckoutTrait {
              extension {
                // Deletes untracked submodules and any other subdirectories which contain .git directories.
                deleteUntrackedNestedRepositories(true)
              }
            }
            cloneOptionTrait {
              extension {
                // Perform shallow clone, so that git will not download the history of the project, saving time and disk space when you just want to access the latest version of a repository.
                shallow(true)
                // Deselect this to perform a clone without tags, saving time and disk space when you just want to access what is specified by the refspec.
                noTags(false)
                // Specify a folder containing a repository that will be used by Git as a reference during clone operations.
                reference("/var/lib/jenkins//apm-pipeline-library.git")
                // Specify a timeout (in minutes) for clone and fetch operations.
                timeout(15)
                // Set shallow clone depth, so that git will only download recent history of the project, saving time and disk space when you just want to access the latest commits of a repository.
                depth(5)
                // Perform initial clone using the refspec defined for the repository.
                honorRefspec(false)
              }
            }
            // Discovers branches on the repository.
            // https://github.com/jenkinsci/github-branch-source-plugin/blob/master/src/main/java/org/jenkinsci/plugins/github_branch_source/BranchDiscoveryTrait.java#L55-L70
            gitHubBranchDiscovery{
              // Determines which branches are discovered.
              strategyId(1)
            }
            // Discovers tags on the repository.
            gitHubTagDiscovery()
            // filers heads
            headRegexFilter {
              // A Java regular expression to restrict the names.
              regex('(master|main|PR-.*|\\d+\\.x)')
            }
            // ignore push
            //ignoreOnPushNotificationTrait()
            // Defines a custom context label to be sent as part of Github Status notifications for this project.
            notificationContextTrait {
              // The text of the context label for Github status notifications.
              contextLabel('apm-shared/oblt-test-env/oblt-test-env-custom-kibana')
              // Appends the relevant suffix to the context label based on the build type.
              typeSuffix(true)
            }
            wipeWorkspaceTrait()
          }
        }
        buildStrategies {
          buildChangeRequests {
            // If the change request / pull request is a merge, there are two reasons for a revision change: The origin of the change request may have changed The target of the change request may ha
            ignoreTargetOnlyChanges(false)
            // Some sources can permit change request / pull request from external entities.
            ignoreUntrustedChanges(true)
          }
          // Builds regular branches whenever a change is detected.
          buildRegularBranches()
          // Builds tags (subject to a configurable tag age time window)
          buildTags {
            atLeastDays('-1')
            // The number of days since the tag was created after which it is no longer eligible for automatic building.
            atMostDays('7')
          }
          // Skip initial build on first branch indexing
          skipInitialBuildOnFirstBranchIndexing()
        }
      }
    }
  }
  // this configuration is broken due https://issues.jenkins.io/browse/JENKINS-63788
  // configure {
  //   // workaround for JENKINS-46202 (https://issues.jenkins-ci.org/browse/JENKINS-46202)
  //   // https://issues.jenkins.io/browse/JENKINS-60874
  //   // Discovers pull requests where the origin repository is the same as the target repository.
  //   // https://github.com/jenkinsci/github-branch-source-plugin/blob/master/src/main/java/org/jenkinsci/plugins/github_branch_source/OriginPullRequestDiscoveryTrait.java#L57-L72
  //   def traits = it / sources / data / 'jenkins.branch.BranchSource' / source / traits
  //   traits << 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait' {
  //     strategyId 1
  //     trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission')
  //   }
  //   traits << 'org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait' {
  //     strategyId 1
  //   }
  // }
  factory {
    workflowBranchProjectFactory {
      scriptPath('.ci/Jenkinsfile')
    }
  }
}
