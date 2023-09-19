module.exports = () => {
  console.log(`Parsing flags`)
  const { cluster_name_prefix, cluster_name_suffix, dry_run, gitops, target, project_type  } = process.env
  if (cluster_name_prefix != '') {
    core.exportVariable('CLUSTER_NAME_PREFIX', `--cluster-name-prefix=${cluster_name_prefix}`)
  }
  if (cluster_name_suffix != '') {
    core.exportVariable('CLUSTER_NAME_SUFFIX', `--cluster-name-suffix=${cluster_name_suffix}`)
  }
  if (dry_run != 'false') {
    core.exportVariable('DRY_RUN', `--dry-run`)
  }
  var parameters = {}
  parameters.ProjectType = project_type
  parameters.Target = target

  if (gitops == 'true') {
    console.log(`Parsing gitops flags`)
    parameters.GitOps = "true"
    parameters.GitHubRepository = `${context.repo.owner}/${context.repo.repo}`
    if (context.eventName == 'issues') {
      if (context.issue.number) {
        parameters.GitHubIssue = context.issue.number
      }
    } else {
      parameters.GitHubPullRequest = context.issue.number
    }
  }
  console.log(`Create PARAMETERS env variable`)
  core.exportVariable('PARAMETERS', `--parameters='${(JSON.stringify(parameters))}'`)
}
