## About

GitHub Action to run the oblt-cli wrapper to create a CCS cluster

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Create ccs cluster using the oblt-cli
on:
  issues:
    types: [opened]
jobs:
  run-oblt-cli:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-create-ccs@current
        with:
          remote-cluster: 'dev-oblt'
          cluster-name-prefix: 'foo'
          token: ${{ secrets.PAT_TOKEN }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name                        | Type    | Default                     | Description                        |
|-----------------------------|---------|-----------------------------|------------------------------------|
| `remote-cluster`            | String  | Mandatory                   | The Oblt cluster to use. |
| `cluster-name-prefix`       | String  | Optional                    | Prefix to be prepended to the randomised cluster name |
| `cluster-name-suffix`       | String  | Optional                    | Suffix to be appended to the randomised cluster name |
| `elasticsearch-docker-image`| String  | Optional                    | Force to use a Docker image for the Elasticserach Deployment |
| `slackChannel`              | String  | `#observablt-bots`          | The slack channel to be configured in the oblt-cli. |
| `token`                     | String  | Mandatory                   | The GitHub token with permissions fetch releases. |
| `username`                  | String  | `apmmachine`                | Username to show in the deployments with oblt-cli, format: [a-z0-9]. |
| `gitops`                    | Boolean | `false`                     | Whether to provide the GitOps metadata to the oblt-cli. |
