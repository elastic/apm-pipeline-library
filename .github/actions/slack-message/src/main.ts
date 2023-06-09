import * as core from '@actions/core'

export async function run(): Promise<void> {
  try {
    // Validate env vars
    let message = process.env.MESSAGE
    const rawPayload = process.env.PAYLOAD
    if ((message == null || message === '') && (rawPayload == null || rawPayload === '')) {
      return core.setFailed('You must define either `MESSAGE` or `PAYLOAD` env var.')
    }
    if (message != null && message !== '' && rawPayload != null && rawPayload !== '') {
      return core.setFailed('You must define only one of the following env var: `MESSAGE` or `PAYLOAD`.')
    }

    // Handle deprecations
    const urlEncoded = process.env.URL_ENCODED
    if (urlEncoded === 'true') {
      core.warning('The `URL_ENCODED` env var is deprecated.')
    }

    // Generate payload
    let payload: any // eslint-disable-line @typescript-eslint/no-explicit-any
    if (message != null) {
      message = sanitizeMessage(message)
      payload = JSON.stringify({
        text: message,
        blocks: [
          {
            type: 'section',
            text: {
              type: 'mrkdwn',
              text: message
            }
          }
        ]
      })
    } else if (rawPayload != null) {
      // Valid json format
      try {
        JSON.parse(rawPayload)
        payload = rawPayload
      } catch (err: unknown) {
        if (err instanceof SyntaxError) {
          return core.setFailed("The `payload` input isn't a valid json document.")
        } else {
          throw err
        }
      }
    }

    // Set output
    core.setOutput('payload', payload)

    // Mask in logs
    const mask = process.env.MASK
    if (mask == null || mask !== 'false') {
      core.setSecret(payload)
    }
    core.info('Succeed to generate payload.')
  } catch (err: unknown) {
    if (err instanceof Error) {
      core.setFailed(err)
    } else {
      core.setFailed('Unhandled error occured')
    }
  }
}

function sanitizeMessage(message: string): string {
  // Support deprecated url encoding
  message = decodeURIComponent(message)

  // Ref: https://api.slack.com/reference/surfaces/formatting#escaping
  message = message.replace('&', '&amp;')
  message = message.replace('<', '&lt;')
  message = message.replace('>', '&gt;')

  // Support multilines
  return message.split(/\r?\n/).join('\n')
}
