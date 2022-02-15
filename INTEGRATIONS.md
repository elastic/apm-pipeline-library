## Support integrations

This is a high level overview of what are the existing integrations we have in place while running tasks in the CI/CD.

Those integrations are related to install the tool if needed and prepare the context where the task will run, this context preparation is likely related to some environment variables that are set or some credentials that are read from vault.


### Integrations with Cloud

|Name|Description|Status|
|----|---|------|
| [`dockerImageExists`](./vars/dockerImageExists.txt) | Interact with docker | :white_check_mark: |
| [`dockerLogin`](./vars/dockerLogin.txt) | Login to docker | :white_check_mark: |
| [`stashV2`](./vars/stashV2.txt) | Stash files in Google Cloud Storage | :white_check_mark: |
| [`unstashV2`](./vars/unstashV2.txt) | Stash files in Google Cloud Storage | :white_check_mark: |
| [`withAWSEnv`](./vars/withAWSEnv.txt) | Interact with AWS | :white_check_mark: |
| [`withAzureCredentials`](./vars/withAzureCredentials.txt) | Interact with Azure | :white_check_mark: |
| [`withAzureEnv`](./vars/withAzureEnv.txt) | Interact with Azure | :white_check_mark: |
| [`withDockerEnv`](./vars/withDockerEnv.txt) | Interact with Docker | :white_check_mark: |
| [`withGCPEnv`](./vars/withGCPEnv.txt) | Interact with Google Cloud | :white_check_mark: |

### Integrations with Artifact Services (releases)

|Name|Description|Status|
|----|---|------|
| [`nexusCloseStagingRepository`](./vars/nexusCloseStagingRepository.txt) | Interact with Nexus | :white_check_mark: |
| [`nexusCreateStagingRepository`](./vars/nexusCreateStagingRepository.txt) | Interact with Nexus | :white_check_mark: |
| [`nexusDropStagingRepository`](./vars/nexusDropStagingRepository.txt) | Interact with Nexus | :white_check_mark: |
| [`nexusFindStagingId`](./vars/nexusFindStagingId.txt) | Interact with Nexus | :white_check_mark: |
| [`nexusReleaseStagingRepository`](./vars/nexusReleaseStagingRepository.txt) | Interact with Nexus | :white_check_mark: |
| [`nexusUploadStagingArtifact`](./vars/nexusUploadStagingArtifact.txt) | Interact with Nexus | :white_check_mark: |
| [`publishToCDN`](./vars/publishToCDN.txt) | Interact with Google | :white_check_mark: |
| [`rubygemsLogin`](./vars/rubygemsLogin.txt) | Interact with RubyGems | :white_check_mark: |

### Integrations with Elastic Stack

|Name|Description|Status|
|----|---|------|
| [`metricbeat`](./vars/metricbeat.txt) | Interact with Metricbeats | :white_check_mark: |
| [`runWatcher`](./vars/runWatcher.txt) | Interact with Elastic | :white_check_mark: |
| [`sendBenchmarks`](./vars/sendBenchmarks.txt) | Interact with Elastic | :white_check_mark: |
| [`sendDataToElasticsearch`](./vars/sendDataToElasticsearch.txt) | Interact with Elastic | :white_check_mark: |
| [`withAPMEnv`](./vars/withAzureCredentials.txt) | Interact with Elastic | :white_check_mark: |
| [`withCloudEnv`](./vars/withCloudEnv.txt) | Interact with Elastic Cloud | :white_check_mark: |
| [`withClusterEnv`](./vars/withClusterEnv.txt) | Interact with Elastic Cloud | :white_check_mark: |

### Integrations with GitHub

|Name|Description|Status|
|----|---|------|
| [`githubApi`](./vars/githubApiCall.txt) | Interact with GitHub | :white_check_mark: |
| [`githubCheck`](./vars/githubCheck.txt) | Interact with GitHub | :white_check_mark: |
| [`githubCommentIssue`](./vars/githubCommentIssue.txt) | Interact with GitHub | :white_check_mark: |
| [`githubCreateIssue`](./vars/githubCreateIssue.txt) | Interact with GitHub | :white_check_mark: |
| [`githubCreatePullRequest`](./vars/githubCreatePullRequest.txt) | Interact with GitHub | :white_check_mark: |
| [`githubEnv`](./vars/githubEnv.txt) | Interact with GitHub | :white_check_mark: |
| [`githubPrComment`](./vars/githubPrComment.txt) | Interact with GitHub | :white_check_mark: |
| [`githubPrExists`](./vars/githubPrExists.txt) | Interact with GitHub | :white_check_mark: |
| [`githubPrInfo`](./vars/githubPrInfo.txt) | Interact with GitHub | :white_check_mark: |
| [`githubPrLabels`](./vars/githubPrLabels.txt) | Interact with GitHub | :white_check_mark: |
| [`githubPrLatestComment`](./vars/githubPrLatestComment.txt) | Interact with GitHub | :white_check_mark: |
| [`githubPrReviews`](./vars/githubPrReviews.txt) | Interact with GitHub | :white_check_mark: |
| [`githubPullRequests`](./vars/githubPullRequests.txt) | Interact with GitHub | :white_check_mark: |
| [`githubReleaseCreate`](./vars/githubReleaseCreate.txt) | Interact with GitHub | :white_check_mark: |
| [`githubReleasePublish`](./vars/githubReleasePublish.txt) | Interact with GitHub | :white_check_mark: |
| [`githubWorkflowRun`](./vars/githubWorkflowRun.txt) | Interact with GitHub | :white_check_mark: |
| [`hasCommentAuthorWritePermissions`](./vars/hasCommentAuthorWritePermissions.txt) | Interact with GitHub | :white_check_mark: |
| [`listGithubReleases`](./vars/listGithubReleases.txt) | Interact with GitHub | :white_check_mark: |
| [`lookForGitHubIssues`](./vars/lookForGitHubIssues.txt) | Interact with GitHub | :white_check_mark: |
| [`matchesPrLabel`](./vars/matchesPrLabel.txt) | Interact with GitHub | :white_check_mark: |
| [`updateGithubCommitStatus`](./vars/updateGithubCommitStatus.txt) | Interact with GitHub | :white_check_mark: |
| [`withGhEnv`](./vars/withGhEnv.txt) | Interact with GitHub | :white_check_mark: |
| [`withGitRelease`](./vars/withGitRelease.txt) | Interact with Git | :white_check_mark: |
| [`withGithubCheck`](./vars/withGithubCheck.txt) | Interact with GitHub | :white_check_mark: |
| [`withGithubNotify`](./vars/withGithubNotify.txt) | Interact with GitHub | :white_check_mark: |
| [`withGithubStatus`](./vars/withGithubStatus.txt) | Interact with GitHub | :white_check_mark: |

### Integrations with Vault

|Name|Description|Status|
|----|---|------|
| [`withSecretVault`](./vars/withSecretVault.txt) | Interact with Vault | :white_check_mark: |
| [`withTotpVault`](./vars/withTotpVault.txt) | Interact with Vault | :white_check_mark: |
| [`withVaultToken`](./vars/withVaultToken.txt) | Interact with Vault | :white_check_mark: |
| [`writeVaultSecret`](./vars/writeVaultSecret.txt) | Interact with Vault | :white_check_mark: |

### Integrations with Tools

|Name|Description|Status|
|----|---|------|
| [`codecov`](./vars/codecov.txt) | Interact with CodeCov | :white_check_mark: |
| [`googleStorageUploadExt`](./vars/googleStorageUploadExt.txt) |  Interact with Google | :white_check_mark: |
| [`gsutil`](./vars/gsutil.txt) | Interact with Google | :white_check_mark: |
| [`preCommit`](./vars/preCommit.txt) | Interact with precommit | :white_check_mark: |
| [`tar`](./vars/tar.txt) | Interact with tar | :white_check_mark: |
| [`tap2Junit`](./vars/tap2Junit.txt) | Interact with docker | :white_check_mark: |
| [`untar`](./vars/untar.txt) | Interact with tar | :white_check_mark: |
| [`withGoEnv`](./vars/withGoEnv.txt) | Interact with GVM | :white_check_mark: |
| [`withMageEnv`](./vars/withMageEnv.txt) | Interact with Go | :white_check_mark: |
| [`withNodeJSEnv`](./vars/withNodeJSEnv.txt) | Interact with Node | :white_check_mark: |
| [`withNpmrc`](./vars/withNpmrc.txt) | Interact with npmrc | :white_check_mark: |
