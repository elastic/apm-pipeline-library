## About

An action that orchestrates the Synthetics Stack E2E tests for a Kibana PR.
This action can only be used in a workflow that is triggered by an `issue_comment` event in elastic/kibana.

E.g.:
  ```
  on:
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
  issue_comment:
    types:
      - created

jobs:
  synthetics-stack-e2e:
    if: ${{ github.event.issue.pull_request && startsWith(github.event.comment.body, '/synthetics-stack-e2e') }}
    runs-on: ubuntu-latest
    env:
      RUN_URL: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}?pr=${{ github.event.issue.number }}
    permissions:
      statuses: write
      pull-requests: write
    steps:
      - name: Get PR head SHA
        id: get-pr-head-sha
        uses: actions/github-script@v6
        with:
          result-encoding: string
          script: |
            const { owner, repo } = context.repo;
            const pull = await github.rest.pulls.get({
              owner,
              repo,
              pull_number: context.issue.number,
            });
            return pull.data.head.sha;
      - name: Create commit status
        uses: actions/github-script@v6
        env:
          SHA: ${{ steps.get-pr-head-sha.outputs.result }}
          JOB_STATUS: ${{ job.status }}
        with:
          script: |
            const { owner, repo } = context.repo;
            github.rest.repos.createCommitStatus({
              owner,
              repo,
              context: "synthetics-stack-e2e",
              sha: process.env.SHA,
              state: 'pending',
              description: "Running synthetics-stack-e2e",
              target_url: "${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}",
            });
      - uses: elastic/apm-pipeline-library/.github/actions/run-synthetics-stack-e2e@current
        with:
          vault-url: ${{ secrets.OBLT_VAULT_ADDR }}
          vault-role-id: ${{ secrets.OBLT_VAULT_ROLE_ID }}
          vault-secret-id: ${{ secrets.OBLT_VAULT_SECRET_ID }}
      - if: always()
        name: Create commit status
        uses: actions/github-script@v6
        env:
          SHA: ${{ steps.get-pr-head-sha.outputs.result }}
          JOB_STATUS: ${{ job.status }}
        with:
          script: |
            const { owner, repo } = context.repo;
            github.rest.repos.createCommitStatus({
              owner,
              repo,
              context: "synthetics-stack-e2e",
              sha: process.env.SHA,
              state: process.env.JOB_STATUS == 'success' ? 'success' : 'failure',
              description: process.env.JOB_STATUS == 'success' ? "The synthetics-stack-e2e tests succeeded" : "synthetics-stack-e2e tests failed",
              target_url: process.env.RUN_URL,
            });
```

## Inputs

Following inputs can be used as `step.with` keys

| Name              | Type   | Required | Description                                                 |
|-------------------|--------|----------|-------------------------------------------------------------|
| `repository`      | String | no       | The git repository to checkout. (Default: `elastic/kibana`) |
| `comment-id`      | String | no       | The GitHub Comment ID                                       |
| `vault-url`       | String | yes      | Vault URL                                                   |
| `vault-role-id`   | String | yes      | Vault role ID                                               |
| `vault-secret-id` | String | yes      | Vault secret ID                                             |
| `pr-number`       | String | no       | The PR number                                               |
| `user`            | String | no       | The GitHub user that triggered the workflow                 |
