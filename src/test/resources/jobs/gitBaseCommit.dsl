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
      includes('git_base_commit')
      buildForkPRHead(true)
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
/*
multibranchPipelineJob('common/jcasc-deploy') {
        factory {
          workflowBranchProjectFactory {
            scriptPath('Jenkinsfile')
          }
        }
        branchSources {
          branchSource {
            source {
              gitSCMSource {
                remote('git@gitlab.com:PROJECT/REPO.git')
                credentialsId('gitlab-key')
                id('jcasc-deploy')
              }
            }
          buildStrategies {
            buildAllBranches {
              strategies {
                skipInitialBuildOnFirstBranchIndexing()
              }
            }
          }
        }
      }
      triggers {
        periodicFolderTrigger {
          interval('1440')
        }
      }
      configure { node ->
        node / sources / data / 'jenkins.branch.BranchSource' / source / traits {
          'jenkins.plugins.git.traits.BranchDiscoveryTrait'()
        }
      }
    }
*/
