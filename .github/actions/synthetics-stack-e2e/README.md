## About

An action that orchestrates the Synthetics Stack E2E tests for a Kibana PR.
This action can only be used in a workflow that is triggered by an `issue_comment` or `pull_request` event.

E.g.:
  ```
  on:
    pull_request_target: ~
    issue_comment:
      types:
        - created
  ```

It first creates a Kibana docker image, then it deploys an ESS cluster with the created docker image,
and finally it runs the Synthetics Stack E2E tests against the deployed ESS cluster.

___

## Example

```yaml
---
name: synthetics

on:
  pull_request_target:
    paths:
      - "x-pack/plugins/synthetics/**"
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
      - uses: elastic/apm-pipeline-library/.github/actions/synthetics-stack-e2e@current
        with:
          vault-url: ${{ secrets.OBLT_VAULT_ADDR }}
          vault-role-id: ${{ secrets.OBLT_VAULT_ROLE_ID }}
          vault-secret-id: ${{ secrets.OBLT_VAULT_SECRET_ID }}
```

## Inputs

Following inputs can be used as `step.with` keys

| Name                          | Type   | Required | Description                                                                                  |
|-------------------------------|--------|----------|----------------------------------------------------------------------------------------------|
| `buildkite-pipeline-slug`     | String | no       | The buildkite pipeline slug. (Default `kibana-pr-synthetics-stack-e2e-ci`)                   |
| `comment-id`                  | String | no       | The GitHub Comment ID. (Default `${{ github.event.comment.id }}`)                            |
| `comment-command`             | String | no       | The literal comment you need to post to trigger the flow. (Default: `/synthetics-stack-e2e`) |
| `context`                     | String | no       | The commit status context. (Default: `synthetics-stack-e2e`)                                 |
| `pr-number`                   | String | no       | The PR number. (Default: `${{ github.event.issue.number }}`)                                 |
| `user`                        | String | no       | The GitHub user that triggered the workflow. (Default: `${{ github.triggering_actor }}`)     |
| `vault-url`                   | String | yes      | Vault URL                                                                                    |
| `vault-role-id`               | String | yes      | Vault role ID                                                                                |
| `vault-secret-id`             | String | yes      | Vault secret ID                                                                              |
