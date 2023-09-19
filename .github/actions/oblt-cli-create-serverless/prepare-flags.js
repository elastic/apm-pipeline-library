const { cluster_name_prefix, cluster_name_suffix, dry_run, gitops, target, project_type  } = process.env
if (cluster_name_prefix != '') {
  core.exportVariable('CLUSTER_NAME_PREFIX', `--cluster-name-prefix==${cluster_name_prefix}`)
}
if (cluster-name-prefix != '') {
  core.exportVariable('CLUSTER_NAME_SUFFIX', `--cluster-name-suffix==${cluster_name_suffix}`)
}
if (dry_run != 'false') {
  core.exportVariable('DRY_RUN', `--dry-run`)
}
var parameters = {}
if (gitops == 'true') {
  parameters.GitOps = "true"
  parameters.GitHubRepository = context.repo
  if (context.eventName == 'issues') {
    if (context.issue.number) {
      parameters.GitHubIssue = context.issue.number
    }
  } else {
    parameters.GitHubCommit = context.repo
    parameters.GitHubPullRequest = context.issue.number  //  github.event.comment.id
  }
}

parameters.ProjectType = project_type
parameters.Target = target

core.exportVariable('PARAMETERS', `--parameters='${(JSON.stringify(parameters))}'`)
