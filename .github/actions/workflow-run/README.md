## About

GitHub Action to run a workflow event from another GitHub repository.

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)
  * [outputs](#outputs)

## Usage

### Configuration

Given the `PAT_TOKEN` GitHub secret when a merge happens in the `main` branch
then trigger a `deploy-my-kibana` GitHub event workflow in the repository `my-org/acme`:

```yaml
---
name: Run GitHub Event Workflow example
on:
  push:
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
          payload: ''
          token: ${{ secrets.PAT_TOKEN }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `repository`      | String  |                             | The GitHub repository for the GitHub event type workflow to be triggered. Format: ORG/REPO . |
| `event`           | String  |                             | The GitHub event type to be triggered. |
| `payload`         | String  |                             | The GitHub event payload to be consumed by the GitHub workflow event type. Format: json |
| `token`           | String  |                             | The GitHub token with permissions to trigger the GitHub workflow event type. |
