NAME = 'it/gitBaseCommit'
multibranchPipelineJob(NAME) {
  branchSources {
    factory {
      workflowBranchProjectFactory {
        scriptPath('Jenkinsfile')
      }
    }
    github {
      id('20200109') // IMPORTANT: use a constant and unique identifier
      scanCredentialsId('2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
      excludes('git_base_commit*')
      repoOwner('v1v')
      repository('its-gitbase')
      buildForkPRHead(false)
      buildForkPRMerge(false)
      buildOriginBranch(true)
      buildOriginBranchWithPR(true)
      buildOriginPRHead(false)
      buildOriginPRMerge(true)
      traits {
        gitHubTagDiscovery()
      }
    }
  }
  orphanedItemStrategy {
    discardOldItems {
      numToKeep(20)
    }
  }
}
