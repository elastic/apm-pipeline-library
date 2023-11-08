import * as core from '@actions/core'
import {actionInputs} from 'github-actions-utils'
import {GraphQLClient, gql} from 'graphql-request'

/**
 * GitHub Actions entrypoint.
 */
export async function run(): Promise<void> {
  try {
    // Validate inputs
    const lock = actionInputs.getBool('lock', true)
    const branch = actionInputs.getString('branch', true)
    const token = actionInputs.getString('token', true, true)
    const owner = actionInputs.getString('owner', true)
    const repo = actionInputs.getString('repo', true)

    // Create github service
    const githubService = new GitHubService(token)
    const branchId = await githubService.searchBranchProtection(owner, repo, branch)
    await githubService.setLock(branchId, lock)
    core.info('Succeed to update branch lock.')
  } catch (err: unknown) {
    if (err instanceof Error) {
      core.setFailed(err)
    } else {
      core.setFailed('Unhandled error occured')
    }
  }
}

/**
 * GitHub service to interact with GitHub GraphQL endpoint.
 */
class GitHubService {
  // GraphQL client
  private client: GraphQLClient

  /**
   * Init github service.
   * @param token github token.
   */
  constructor(token: string) {
    this.client = new GraphQLClient('https://api.github.com/graphql', {
      headers: {
        authorization: `Bearer ${token}`
      }
    })
  }

  async searchBranchProtection(owner: string, repo: string, branch: string): Promise<string> {
    const query = gql`
      query searchBranchProtection($repo: String!, $owner: String!) {
        repository(name: $repo, owner: $owner) {
          branchProtectionRules(first: 100) {
            nodes {
              id
              pattern
            }
          }
        }
      }
    `

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const data: any = await this.client.request(query, {repo, owner})
    for (const node of data.repository.branchProtectionRules.nodes) {
      const pattern = new RegExp(node.pattern)
      if (pattern.test(branch)) {
        return node.id
      }
    }
    throw new BranchNotFound(branch)
  }

  /**
   * Set lock on branch protections.
   * @param branchId branch id ref.
   * @param lock lock or unlock branch.
   */
  async setLock(branchId: string, lock: boolean): Promise<void> {
    const query = gql`
      mutation setLock($branchId: ID!, $lock: Boolean!) {
        updateBranchProtectionRule(input: {branchProtectionRuleId: $branchId, lockBranch: $lock}) {
          clientMutationId
        }
      }
    `

    await this.client.request(query, {branchId, lock})
  }
}

/**
 * Represents a branch not found error.
 */
class BranchNotFound extends Error {
  /**
   * Init branch not found error.
   * @param branchName branch name.
   */
  constructor(branchName: string) {
    super(`Failed to find the branch: ${branchName}`)
  }
}
