import {expect, describe, beforeEach, it, jest} from '@jest/globals'
import {RunTarget, RunOptions} from 'github-action-ts-run-api'
import {run} from '../../src/main'

const itIf = (condition: boolean, ...args: Parameters<typeof test>) => (condition ? test(...args) : test.skip(...args))

describe('GitHub Action', () => {
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
  })

  it('should fail if `lock` isn`t present', async () => {
    const result = await RunTarget.asyncFn(run).run(commonOptions.clone())
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('Error: Input required and not supplied: lock')).toStrictEqual(true)
  })

  it('should fail if `branch` isn`t present', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setInputs({
        lock: 'true'
      })
    )
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('Error: Input required and not supplied: branch')).toStrictEqual(true)
  })

  it('should fail if `token` isn`t present', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setInputs({
        lock: 'true',
        branch: 'foobar'
      })
    )
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('Error: Input required and not supplied: token')).toStrictEqual(true)
  })

  it('should fail if `owner` isn`t present', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setInputs({
        lock: 'true',
        branch: 'foobar',
        token: 'gh_token'
      })
    )
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('Error: Input required and not supplied: owner')).toStrictEqual(true)
  })

  it('should fail if `repo` isn`t present', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setInputs({
        lock: 'true',
        branch: 'foobar',
        token: 'gh_token',
        owner: 'github'
      })
    )
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('Error: Input required and not supplied: repo')).toStrictEqual(true)
  })

  itIf(process.env.GITHUB_TOKEN != null, 'should fail if branch doesn`t exist', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setInputs({
        lock: 'true',
        branch: 'foobar',
        token: process.env.GITHUB_TOKEN,
        owner: 'elastic',
        repo: 'apm-pipeline-library'
      })
    )
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('Error: Failed to find the branch: foobar')).toStrictEqual(true)
  })

  itIf(process.env.GITHUB_TOKEN != null, 'should succeed to lock branch', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setInputs({
        lock: 'true',
        branch: 'test-ga-branch-lock',
        token: process.env.GITHUB_TOKEN,
        owner: 'elastic',
        repo: 'apm-pipeline-library'
      })
    )
    expect(result.isSuccess).toStrictEqual(true)
  })

  itIf(process.env.GITHUB_TOKEN != null, 'should succeed to unlock branch', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setInputs({
        lock: 'false',
        branch: 'test-ga-branch-lock',
        token: process.env.GITHUB_TOKEN,
        owner: 'elastic',
        repo: 'apm-pipeline-library'
      })
    )
    expect(result.isSuccess).toStrictEqual(true)
  })
})
