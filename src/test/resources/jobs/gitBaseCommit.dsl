NAME = 'it/gitBaseCommit'
multibranchPipelineJob(NAME) {
  branchSources {
    factory {
      workflowBranchProjectFactory {
        scriptPath('resources/gitBaseCommit.groovy')
      }
    }
    github {
      id('20200109') // IMPORTANT: use a constant and unique identifier
      scanCredentialsId('2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
      repoOwner('elastic')
      repository('apm-pipeline-library')
      buildForkPRHead(false)
      buildForkPRMerge(true)
      buildOriginBranch(true)
      buildOriginBranchWithPR(true)
      buildOriginPRHead(false)
      buildOriginPRMerge(false)
    }
  }
  orphanedItemStrategy {
    discardOldItems {
      numToKeep(20)
    }
  }
}
