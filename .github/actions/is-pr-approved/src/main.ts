import * as core from '@actions/core'
import {Context} from '@actions/github/lib/context'
import {GitHub} from '@actions/github/lib/utils'
import {actionInputs, transformIfSet} from 'github-actions-utils'

interface GitHubAPIError {
  status: number
  request: any // eslint-disable-line @typescript-eslint/no-explicit-any
  response: any // eslint-disable-line @typescript-eslint/no-explicit-any
}

export const DEFAULT_ALLOWED_ACTORS: readonly string[] = [
  'greenkeeper[bot]',
  'dependabot[bot]',
  'mergify[bot]',
  'github-actions[bot]'
]

export async function run(): Promise<void> {
  // Use dynamic import to let `github-action-ts-run-api` library inject
  // Github Context before using it.
  // Static import will not work and result in empty Github Context
  const github = await import('@actions/github')

  try {
    // Validate event source
    const eventName = github.context.eventName
    if (!['pull_request', 'pull_request_target'].includes(eventName)) {
      return core.info(`Skip validation for events other than 'pull_request' and 'pull_request_target' (${eventName})`)
    }

    // Get the Github token from the environment
    const githubToken = actionInputs.getString('github-token', false) || process.env.GITHUB_TOKEN
    if (githubToken == null) {
      return core.setFailed('Unable to retrieve a valid `GITHUB_TOKEN`')
    } else {
      core.setSecret(githubToken)
    }

    // Create an Octokit client with the user's access token
    const octokit = github.getOctokit(githubToken)

    // Perform all checks
    const isKnownActor = checkKnownActor(github.context.actor)
    const userHasPermission = await checkActorPermission(octokit, github.context)
    const isMemberOfOrg = await checkMemberOfOrg(octokit, github.context)
    const isPRReviewed = await checkPRReviewed(octokit, github.context)

    // Compute approval in order
    const isPRApproved = isKnownActor || userHasPermission || isMemberOfOrg || isPRReviewed

    // Check if PR is approved
    if (isPRApproved) {
      core.info('The PR is approved')
    } else {
      core.setFailed("The PR isn't approved")
    }
  } catch (err) {
    if (err instanceof Error) {
      core.setFailed(err)
    } else {
      core.setFailed('Unhandled error occured')
    }
  }
}

async function checkPRReviewed(octokit: InstanceType<typeof GitHub>, context: Context): Promise<boolean> {
  // Get repository information from the environment
  const repository = context.repo

  // This use case shouldn't exist since we support only `pull_request` and `pull_request_target` events
  // We still need to check if the pull request id is defined
  const pullRequestId = context.payload.pull_request?.number || -1
  if (pullRequestId < 0) return false

  try {
    // List all reviews
    // Ref: https://docs.github.com/en/rest/pulls/reviews?apiVersion=2022-11-28#list-reviews-for-a-pull-request
    let page = 1
    while (page > 0) {
      // Fetch reviews
      const {data: reviews} = await octokit.rest.pulls.listReviews({
        ...repository,
        pull_number: pullRequestId,
        page
      })

      // No more reviews to process
      if (reviews.length === 0) return false

      // Filter approved reviews for the specific commit
      const approvedReviews = reviews.filter(review => {
        return (
          review.commit_id === context.sha &&
          review.state === 'APPROVED' &&
          // Exclude billing manager
          ['MEMBER', 'OWNER', 'COLLABORATOR'].includes(review.author_association)
        )
      })

      // A trusted user has allowed the PR with the specific commit
      if (approvedReviews.length > 0) return true

      // Next page
      page += 1
    }
    return false
  } catch (err) {
    return false
  }
}

async function checkActorPermission(octokit: InstanceType<typeof GitHub>, context: Context): Promise<boolean> {
  // Get repository information from the environment
  const repository = context.repo

  try {
    // Check repository permissions for user
    // Ref: https://docs.github.com/en/rest/collaborators/collaborators?apiVersion=2022-11-28#get-repository-permissions-for-a-user
    const {data: permissionsLevel} = await octokit.rest.repos.getCollaboratorPermissionLevel({
      ...repository,
      username: context.actor
    })

    // Check if the user has admin/write permissions on the repository
    return ['admin', 'write'].includes(permissionsLevel.permission)
  } catch (err) {
    return false
  }
}

function checkKnownActor(actor: string): boolean {
  // Get allowed actors from the environment
  let allowedActors: readonly string[] | undefined = transformIfSet(
    actionInputs.getString('allowed-actors', false),
    actors => actors.split(',').map(allowedActor => allowedActor.trim())
  )
  if (allowedActors == null) {
    allowedActors = DEFAULT_ALLOWED_ACTORS
  }

  // Check
  return allowedActors.includes(actor)
}

async function checkMemberOfOrg(octokit: InstanceType<typeof GitHub>, context: Context): Promise<boolean> {
  try {
    // Check actor is member of org
    // Ref: https://docs.github.com/en/rest/orgs/members?apiVersion=2022-11-28#get-organization-membership-for-a-user
    const {data} = await octokit.rest.orgs.getMembershipForUser({
      org: context.repo.owner,
      username: context.actor
    })
    return data.state === 'active' && ['admin', 'member'].includes(data.role)
  } catch (err) {
    const apiError = err as GitHubAPIError
    if (apiError.status === 403) {
      core.debug("The `GITHUB_TOKEN` haven't enough permissions")
    }
    return false
  }
}
