NAME = 'it/gitBaseCommit'
multibranchPipelineJob(NAME) {
  branchSources {
    factory {
      workflowBranchProjectFactory {
        scriptPath('Jenkinsfile')
      }
    }
    branchSource {
      source {
        github {
          id('20200109') // IMPORTANT: use a constant and unique identifier
          credentialsId('2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
          repoOwner('v1v')
          repository('its-gitbase')
          repositoryUrl('https://github.com/v1v/its-gitbase')
          configuredByUrl(false)
          traits {
            gitHubTagDiscovery()
            gitHubBranchDiscovery {
              strategyId(1)
            }
            gitHubPullRequestDiscovery {
              strategyId(1)
            }
            headWildcardFilter {
              includes('*')
              excludes('git_base_commit*')
            }
          }
        }
      }
      buildStrategies {
        skipInitialBuildOnFirstBranchIndexing()
      }
    }
  }
  orphanedItemStrategy {
    discardOldItems {
      numToKeep(20)
    }
  }
}
