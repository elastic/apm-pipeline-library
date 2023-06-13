import {expect, describe, beforeEach, it, jest} from '@jest/globals'
import {RunTarget, RunOptions} from 'github-action-ts-run-api'
import {run} from '../../src/main'

describe('validate env vars', () => {
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

  it.each([['PULL_REQUEST_ID'], ['PULL_REQUEST_SHA'], ['JOB_STATUS'], ['MESSAGE']])(
    'should failed if env var `%s` is absent',
    async (envVar: string): Promise<void> => {
      const envVars: {[name: string]: string} = {
        GITHUB_RUN_ATTEMPT: '1',
        GITHUB_REF_NAME: 'feature-branch-1',
        GITHUB_SHA: '6233871ea2db9ddc0b5e627fafc52a15c77756d9',
        PULL_REQUEST_ID: '2',
        PULL_REQUEST_SHA: '6233871ea2db9ddc0b5e627fafc52a15c77756d7',
        JOB_STATUS: 'success',
        MESSAGE: 'foobar'
      }
      delete envVars[envVar]

      const result = await RunTarget.asyncFn(run).run(commonOptions.clone().setEnv(envVars))
      expect(result.isSuccess).toStrictEqual(false)
      expect(result.commands.errors?.includes(`Error: The env var '${envVar}' isn't defined.`)).toStrictEqual(true)
    }
  )
})

describe('check wrapper', () => {
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

  it('should succeed with outputs', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        GITHUB_RUN_ATTEMPT: '1',
        GITHUB_REF_NAME: 'feature-branch-1',
        GITHUB_SHA: '6233871ea2db9ddc0b5e627fafc52a15c77756d9',
        PULL_REQUEST_ID: '2',
        PULL_REQUEST_SHA: '6233871ea2db9ddc0b5e627fafc52a15c77756d7',
        JOB_STATUS: 'success',
        MESSAGE: `foo
bar`
      })
    )

    expect(result.isSuccess).toStrictEqual(true)
    expect(result.commands.outputs.color).toEqual('good')
    expect(result.commands.outputs.short_sha).toEqual('6233871')
    expect(result.commands.outputs.run_url).toMatch(
      /https:\/\/github\.com\/org\/repo\/actions\/runs\/([0-9]+)\/attempts\/1/
    )
    expect(result.commands.outputs.repo_url).toEqual('https://github.com/org/repo')
    const rawSlackPayload = result.commands.outputs.slackPayload
    expect(rawSlackPayload).toBeDefined()
    const payload = JSON.parse(rawSlackPayload as string)
    expect(payload.attachments[0].text).toEqual('foo\nbar')
  })
})
