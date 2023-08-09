## About

GitHub Action to run the oblt-cli wrapper to create a custom cluster

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Create custom cluster using the oblt-cli
on:
  issues:
    types: [opened]
jobs:
  run-oblt-cli:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-create-custom@current
        with:
          template: 'deploy-kibana'
          cluster-name-prefix: 'foo'
          parameters: '{"RemoteClusterName":"release-oblt","StackVersion":"8.7.0","ElasticsearchDockerImage":"docker.elastic.co/observability-ci/elasticsearch-cloud-ess:8.7.0-046d305b","KibanaDockerImage":"docker.elastic.co/observability-ci/kibana-cloud:8.7.0-SNAPSHOT-87"}'
          token: ${{ secrets.PAT_TOKEN }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name                        | Type    | Default                     | Description                        |
|-----------------------------|---------|-----------------------------|------------------------------------|
| `template`                  | String  | Mandatory                   | The Oblt cluster to use. |
| `parameters`                | String  | Mandatory                   | Parameters values defined in JSON |
| `token`                     | String  | Mandatory                   | The GitHub token with permissions fetch releases. |
| `cluster-name-prefix`       | String  | Optional                    | Prefix to be prepended to the randomised cluster name |
| `cluster-name-suffix`       | String  | Optional                    | Suffix to be appended to the randomised cluster name |
| `skip-random-name`          | Boolean | `false`                      | Whether to skip the randomised cluster name |
| `dry-run`                   | Boolean | `false`                     | Whether to dry-run the oblt-cli. |
| `slackChannel`              | String  | `#observablt-bots`          | The slack channel to be configured in the oblt-cli. |
| `username`                  | String  | `apmmachine`                | Username to show in the deployments with oblt-cli, format: [a-z0-9]. |
| `gitops`                    | Boolean | `false`                     | Whether to provide the GitOps metadata to the oblt-cli. |
