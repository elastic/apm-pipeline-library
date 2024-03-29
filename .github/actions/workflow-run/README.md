## About

GitHub Action to run a workflow event from another GitHub repository.

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the repository_dispatcher wokflow called `deploy my kibana`
  And the `PAT_TOKEN` GitHub secret
When a Pull Request happens that targets the `main` branch
Then it triggers a `deploy-my-kibana` GitHub event workflow in the repository `my-org/acme`:

```yaml
---
name: Run GitHub Event Workflow example
on:
  pull_request:
    branches:
      - main
jobs:
  run-workflow:
    name: Dispatch workflow
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/workflow-run@current
        with:
          repository: "my-org/acme"
          event: 'deploy-my-kibana'
          payload: '{ "ref": "${{ github.event.pull_request.head.sha }}", repository: "${{ github.repository }}" }'
          token: ${{ secrets.PAT_TOKEN }}
```

```yaml
---
name: deploy my kibana

on:
  repository_dispatch:
    types:
      - deploy-my-kibana

jobs:
  dispatcher:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          ref: ${{ github.event.client_payload.ref }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `repository`      | String  |                             | The GitHub repository for the GitHub event type workflow to be triggered. Format: ORG/REPO . |
| `event`           | String  |                             | The GitHub event type to be triggered. |
| `payload`         | String  |                             | The GitHub event payload to be consumed by the GitHub workflow event type. Format: JSON ( { "my-key": "my-value", ... } ). |
| `token`           | String  |                             | The GitHub token with permissions to trigger the GitHub workflow event type. |
