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

  it('should fail if `MESSAGE` and `PAYLOAD` are present', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: 'foobar',
        PAYLOAD: JSON.stringify({})
      })
    )
    expect(result.isSuccess).toStrictEqual(false)
    expect(
      result.commands.errors?.includes('You must define only one of the following env var: `MESSAGE` or `PAYLOAD`.')
    ).toStrictEqual(true)
  })

  it('should fail if `MESSAGE` is empty', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: ''
      })
    )
    expect(result.isSuccess).toStrictEqual(false)
    expect(result.commands.errors?.includes('You must define either `MESSAGE` or `PAYLOAD` env var.')).toStrictEqual(
      true
    )
  })

  it('should warn if `URL_ENCODED` is defined', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: 'foobar',
        URL_ENCODED: 'true'
      })
    )
    expect(result.isSuccess).toStrictEqual(true)
    expect(result.commands.warnings?.includes('The `URL_ENCODED` env var is deprecated.')).toStrictEqual(true)
  })
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

  it('should succeed if `MESSAGE` contain multiline', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: `foo
bar`
      })
    )

    expect(result.isSuccess).toStrictEqual(true)
    const rawPayload = result.commands.outputs.payload
    expect(rawPayload).toBeDefined()
    const payload = JSON.parse(rawPayload as string)
    expect(payload.blocks[0].text.text).toEqual('foo\nbar')
  })

  it('should succeed if `MESSAGE` is url encoded', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: '%E4%B8%8A%E6%B5%B7%2B%E4%B8%AD%E5%9C%8B'
      })
    )

    expect(result.isSuccess).toStrictEqual(true)
    const rawPayload = result.commands.outputs.payload
    expect(rawPayload).toBeDefined()
    const payload = JSON.parse(rawPayload as string)
    expect(payload.blocks[0].text.text).toEqual('上海+中國')
  })

  it('should succeed if `MESSAGE` contain control characters', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: '<&>'
      })
    )

    expect(result.isSuccess).toStrictEqual(true)
    const rawPayload = result.commands.outputs.payload
    expect(rawPayload).toBeDefined()
    const payload = JSON.parse(rawPayload as string)
    expect(payload.blocks[0].text.text).toEqual('&lt;&amp;&gt;')
  })

  it('should succeed to mask payload by default', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: 'foobar'
      })
    )

    expect(result.isSuccess).toStrictEqual(true)
    const rawPayload = result.commands.outputs.payload
    expect(rawPayload).toBeDefined()
    expect(result.commands.secrets.includes(rawPayload as string)).toStrictEqual(true)
  })

  it('should succeed to mask payload when explicit', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: 'foobar',
        MASK: 'true'
      })
    )

    expect(result.isSuccess).toStrictEqual(true)
    const rawPayload = result.commands.outputs.payload
    expect(rawPayload).toBeDefined()
    expect(result.commands.secrets.includes(rawPayload as string)).toStrictEqual(true)
  })

  it('should succeed to not mask payload when explicit', async () => {
    const result = await RunTarget.asyncFn(run).run(
      commonOptions.clone().setEnv({
        MESSAGE: 'foobar',
        MASK: 'false'
      })
    )

    expect(result.isSuccess).toStrictEqual(true)
    const rawPayload = result.commands.outputs.payload
    expect(rawPayload).toBeDefined()
    expect(result.commands.secrets.includes(rawPayload as string)).toStrictEqual(false)
  })
})
