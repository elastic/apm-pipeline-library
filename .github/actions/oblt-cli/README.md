## About

GitHub Action to run the oblt-cli wrapper

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Create cluster using the oblt-cli
on:
  issues:
    types: [opened]
jobs:
  run-oblt-cli:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli@current
        with:
          command: 'cluster create ccs --remote-cluster=dev-oblt --cluster-name-prefix mycustomcluster'
          token: ${{ secrets.PAT_TOKEN }}
```


## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `command`         | String  |                             | The oblt-cli command to run, without the oblt-cli prefix. |
| `slackChannel`    | String  | `#observablt-bots`          | The slack channel to be configured in the oblt-cli. |
| `token`           | String  |                             | The GitHub token with permissions fetch releases. |
| `username`        | String  | `apmmachine`                | Username to show in the deployments with oblt-cli, format: [a-z0-9]. |
