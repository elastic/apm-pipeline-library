NAME = 'it/k8s/apm-pipeline-library'
multibranchPipelineJob(NAME) {
  branchSources {
    factory {
      workflowBranchProjectFactory {
        scriptPath('.ci/Jenkinsfile')
      }
    }
    branchSource {
      source {
        github {
          id('202001091') // IMPORTANT: use a constant and unique identifier
          credentialsId('2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
          repoOwner('elastic')
          repository('apm-pipeline-library')
          repositoryUrl('https://github.com/elastic/apm-pipeline-library')
          configuredByUrl(false)
          traits {
            gitHubPullRequestDiscovery {
              strategyId(1)
            }
            headWildcardFilter {
              includes('*705')
              excludes('*')
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
