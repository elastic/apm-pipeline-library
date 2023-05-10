import {expect, describe, beforeEach, afterEach, it, jest} from '@jest/globals'
import {RunTarget, RunOptions} from 'github-action-ts-run-api'
import path from 'path'
import nock, {Scope} from 'nock'
import {DEFAULT_ALLOWED_ACTORS, run} from '../../src/main'

function resolveFixture(name: string): string {
  return path.resolve(process.cwd(), `tests/fixtures/${name}.json`)
}

describe('check missing inputs', () => {
  const commonOptions = RunOptions.create()
    .setGithubContext({
      eventName: 'pull_request',
      repository: 'org/repo'
    })
    .setOutputOptions({
      printStdout: false,
      printStderr: false
    })
    .setShouldFakeMinimalGithubRunnerEnv(true)

  beforeEach(() => {
    jest.resetModules()
    nock.disableNetConnect()
  })

  afterEach(() => {
    nock.cleanAll()
    nock.enableNetConnect()
  })

  it("should fail if github-token isn't present", async () => {
    const result = await RunTarget.asyncFn(run).run(commonOptions)
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('Unable to retrieve a valid `GITHUB_TOKEN`')).toStrictEqual(true)
  })

  it('should succeed if the github action is trigger on `push`', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setGithubContext({
        eventName: 'push'
      })
    )
    expect(result.isSuccess).toStrictEqual(true)
  })
})

describe('check pr is approved', () => {
  const commonOptions = RunOptions.create()
    .setGithubContext({
      eventName: 'pull_request',
      repository: 'org/repo',
      actor: 'octocat',
      payload: {
        pull_request: {
          number: 10
        }
      },
      sha: 'ecdd80bb57125d7ba9641ffaa4d7d2c19d3f3091'
    })
    .setOutputOptions({
      printStdout: false,
      printStderr: false
    })
    .setInputs({'github-token': 'xxxxxxxxxxxxxxxx'})
    .setShouldFakeMinimalGithubRunnerEnv(true)

  interface ScopeOptions {
    withoutPermission?: boolean
    withoutMembership?: boolean
    withoutReviews?: boolean
  }

  function defaultScope({
    withoutPermission = false,
    withoutMembership = false,
    withoutReviews = false
  }: ScopeOptions): Scope {
    let scope = nock('https://api.github.com')
    if (!withoutPermission) {
      scope
        .get('/repos/org/repo/collaborators/octocat/permission')
        .replyWithFile(200, resolveFixture('repos-collaborators-no-permission'), {
          'Content-Type': 'application/json'
        })
    }
    if (!withoutMembership) {
      scope.get('/orgs/org/memberships/octocat').reply(404)
    }
    if (!withoutReviews) {
      scope
        .get('/repos/org/repo/pulls/10/reviews')
        .query({
          page: 1
        })
        .reply(200, [])
    }
    return scope
  }

  beforeEach(() => {
    jest.resetModules()
    nock.disableNetConnect()
  })

  afterEach(() => {
    nock.cleanAll()
    nock.enableNetConnect()
  })

  it("should fail if user isn't allowed", async () => {
    const scope = defaultScope({})
    const result = await RunTarget.asyncFn(run).run(commonOptions)
    expect(result.isSuccess).toStrictEqual(false)
    expect(scope.isDone())
  })

  it.each([['write'], ['admin']])('should succeed if user has %s permission', async (role: string): Promise<void> => {
    const scope = defaultScope({withoutPermission: true})
      .get('/repos/org/repo/collaborators/octocat/permission')
      .replyWithFile(200, resolveFixture(`repos-collaborators-${role}-permission`), {
        'Content-Type': 'application/json'
      })

    const result = await RunTarget.asyncFn(run).run(commonOptions)
    expect(result.isSuccess).toStrictEqual(true)
    expect(scope.isDone())
  })

  it("should fail if user isn't know", async () => {
    const scope = defaultScope({})
    const result = await RunTarget.asyncFn(run).run(commonOptions)
    expect(result.isSuccess).toStrictEqual(false)
    expect(scope.isDone())
  })

  it.each(DEFAULT_ALLOWED_ACTORS.map(v => [v]))(
    'should succeed if user is known (%s)',
    async (user: string): Promise<void> => {
      const scope = defaultScope({})
      const result = await RunTarget.asyncFn(run).run(
        commonOptions.clone().setGithubContext({
          actor: user
        })
      )
      expect(result.isSuccess).toStrictEqual(true)
      expect(scope.isDone())
    }
  )

  it("should fail if user isn't member of org", async () => {
    const scope = defaultScope({})
    const result = await RunTarget.asyncFn(run).run(commonOptions)
    expect(result.isSuccess).toStrictEqual(false)
    expect(scope.isDone())
  })

  it('should succeed if user is member of org', async () => {
    const scope = defaultScope({withoutMembership: true})
      .get('/orgs/org/memberships/octocat')
      .replyWithFile(200, resolveFixture('orgs-memberships'), {
        'Content-Type': 'application/json'
      })

    const result = await RunTarget.asyncFn(run).run(commonOptions)
    expect(result.isSuccess).toStrictEqual(true)
    expect(scope.isDone())
  })

  it("should fail if a review isn't approved", async () => {
    const scope = defaultScope({withoutReviews: true})
      .get('/repos/org/repo/pulls/10/reviews')
      .query({
        page: 1
      })
      .replyWithFile(200, resolveFixture('repos-pulls-reviews-page-1'), {
        'Content-Type': 'application/json'
      })
      .get('/repos/org/repo/pulls/10/reviews')
      .query({
        page: 2
      })
      .reply(200, [])

    const result = await RunTarget.asyncFn(run).run(commonOptions)
    expect(result.isSuccess).toStrictEqual(false)
    expect(scope.isDone())
  })

  it('should succeed if a review is approved', async () => {
    const scope = defaultScope({withoutReviews: true})
      .get('/repos/org/repo/pulls/10/reviews')
      .query({
        page: 1
      })
      .replyWithFile(200, resolveFixture('repos-pulls-reviews-page-1'), {
        'Content-Type': 'application/json'
      })
      .get('/repos/org/repo/pulls/10/reviews')
      .query({
        page: 2
      })
      .replyWithFile(200, resolveFixture('repos-pulls-reviews-page-2'), {
        'Content-Type': 'application/json'
      })
      .get('/repos/org/repo/pulls/10/reviews')
      .query({
        page: 3
      })
      .reply(200, [])

    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setGithubContext({
        payload: {
          pull_request: {
            number: 10
          }
        },
        sha: 'ecdd80bb57125d7ba9641ffaa4d7d2c19d3f3091'
      })
    )
    expect(result.isSuccess).toStrictEqual(true)
    expect(scope.isDone())
  })
})
