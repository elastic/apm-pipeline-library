import * as core from '@actions/core'

export async function run(): Promise<void> {
  try {
    // Validate env vars
    const githubActor = checkEnvVar('GITHUB_ACTOR')
    const githubEventName = checkEnvVar('GITHUB_EVENT_NAME')
    const githubRefName = checkEnvVar('GITHUB_REF_NAME')
    const githubRepository = checkEnvVar('GITHUB_REPOSITORY')
    const githubRunAttempt = checkEnvVar('GITHUB_RUN_ATTEMPT')
    const githubRunId = checkEnvVar('GITHUB_RUN_ID')
    const githubServerUrl = checkEnvVar('GITHUB_SERVER_URL')
    const githubSha = checkEnvVar('GITHUB_SHA')
    const githubWorkflow = checkEnvVar('GITHUB_WORKFLOW')
    const jobStatus = checkEnvVar('JOB_STATUS')
    const message = process.env.MESSAGE || undefined
    const pullRequestId = process.env.PULL_REQUEST_ID || ''
    const pullRequestSha = process.env.PULL_REQUEST_SHA || ''

    // Compute values
    const repoUrl = `${githubServerUrl}/${githubRepository}`
    const isPullRequest = ['pull_request', 'pull_request_target'].includes(githubEventName)
    const sha = isPullRequest ? pullRequestSha : githubSha
    const shortSha = sha.slice(0, 7)
    const commitUrl = isPullRequest ? `${repoUrl}/pull/${pullRequestId}/commits/${sha}` : `${repoUrl}/commit/${sha}`
    const runUrl = `${repoUrl}/actions/runs/${githubRunId}/attempts/${githubRunAttempt}`
    const color = colorByJobStatus(jobStatus)

    // Set output
    core.setOutput('color', color)
    core.setOutput('short_sha', shortSha) // 7 first characters
    core.setOutput('commit_url', commitUrl)
    core.setOutput('run_url', runUrl)
    core.setOutput('repo_url', repoUrl)
    core.setOutput(
      'slackPayload',
      JSON.stringify({
        attachments: [
          {
            pretext: `Workflow <${repoUrl}/actions/runs/${githubRunId}|${githubWorkflow}> triggered by <${githubServerUrl}/${githubActor}|${githubActor}>`,
            color,
            text: message,
            fields: [
              {
                title: 'Status',
                short: true,
                value: jobStatus
              },
              {
                title: 'Event',
                short: true,
                value: githubEventName
              },
              {
                title: 'Ref',
                short: true,
                value: githubRefName
              },
              {
                title: 'Commit',
                short: true,
                value: `<${commitUrl}|${shortSha}>`
              },
              {
                title: 'Workflow',
                short: true,
                value: `<${runUrl}|${githubWorkflow} #${githubRunId}>`
              }
            ],
            footer: `<${repoUrl}|${githubRepository}>`,
            footer_icon:
              'https://slack-imgs.com/?c=1&o1=wi32.he32.si&url=https%3A%2F%2Fslack.github.com%2Fstatic%2Fimg%2Ffavicon-neutral.png'
          }
        ]
      })
    )
  } catch (err: unknown) {
    if (err instanceof Error) {
      core.setFailed(err)
    } else {
      core.setFailed('Unhandled error occured')
    }
  }
}

function checkEnvVar(envVar: string): string {
  const value = process.env[envVar]
  if (value == null || value === '') {
    throw new Error(`The env var '${envVar}' isn't defined.`)
  }
  return value
}

function colorByJobStatus(status: string): string {
  if (status === 'success') {
    return 'good'
  } else if (status === 'failure') {
    return 'danger'
  } else {
    return ''
  }
}
