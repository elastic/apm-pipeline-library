## About

This action is part of the synthetics-stack-e2e action.

___

## Example

```yaml
---
name: synthetics

on:
  issue_comment:
    types:
      - created

jobs:
  synthetics-stack-e2e:
    runs-on: ubuntu-latest
    permissions:
      statuses: write
      pull-requests: write
    steps:
      - uses: ./.github/actions/synthetics-stack-e2e-run
        with:
          buildkite-pipeline-slug: ${{ inputs.buildkite-pipeline-slug }}
          commit-sha: ${{ steps.get-pr-head-sha.outputs.result }}
          comment-id: ${{ inputs.comment-id }}
          pr-number: ${{ inputs.pr-number }}
          user: ${{ inputs.user }}
          vault-url: ${{ inputs.vault-url }}
          vault-role-id: ${{ inputs.vault-role-id }}
          vault-secret-id: ${{ inputs.vault-secret-id }}
```

## Inputs

Following inputs can be used as `step.with` keys

| Name              | Type   | Required | Description                                                 |
|-------------------|--------|----------|-------------------------------------------------------------|
| `vault-url`       | String | yes      | Vault URL                                                   |
| `vault-role-id`   | String | yes      | Vault role ID                                               |
| `vault-secret-id` | String | yes      | Vault secret ID                                             |
| `comment-id`      | String | no       | The GitHub Comment ID                                       |
| `pr-number`       | String | no       | The PR number                                               |
| `user`            | String | no       | The GitHub user that triggered the workflow                 |
