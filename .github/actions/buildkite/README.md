## About

GitHub Action to run a BuildKite pipeline using Vault

___

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration


```yaml
---
name: Run In BuildKite
on:
  workflow_run:
    workflows:
      - ci
    types: [ completed ]

jobs:
  build-sign:
    timeout-minutes: 5
    runs-on: ubuntu-latest

    steps:

      - name: Run BuildKite pipeline
        id: buildkite
        uses: elastic/apm-pipeline-library/.github/actions/buildkite@current
        with:
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}
          pipeline: observability-release-helm
          buildEnvVars: |
            commit=abderg
            message=my-message
            org=my-org
            something=my super duper variable

      - if: ${{ success() }}
        name: Report BuildKite build in slack
        uses: elastic/apm-pipeline-library/.github/actions/slack-message@current
        with:
          url: ${{ secrets.VAULT_ADDR }}
          roleId: ${{ secrets.VAULT_ROLE_ID }}
          secretId: ${{ secrets.VAULT_SECRET_ID }}
          channel: "#my-channel"
          message: "Buildkite: (<${{ steps.buildkite.outputs.build }}|build>)"

```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name                        | Type    | Default                                             | Description                                                                                                       |
|-----------------------------|---------|-----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `vaultRoleId`               | String  |                                                     | The Vault role id.                                                                                                |
| `vaultSecretId`             | String  |                                                     | The Vault secret id.                                                                                              |
| `vaultUrl`                  | String  |                                                     | The Vault URL to connect to.                                                                                      |
| `secret`                    | String  | `secret/observability-team/ci/buildkite-automation` | The Vault secret.                                                                                                 |
| `org`                       | String  | `elastic`                                           | The Buildkite org.                                                                                                |
| `pipeline`                  | String  |                                                     | The Buildkite pipeline to interact with.                                                                          |
| `pipelineVersion`           | String  | `HEAD`                                              | The Buildkite pipeline version to be used, git tag, commit or branch.                                             |
| `triggerMessage`            | String  | `Triggered automatically with GH actions`           | The Buildkite build message to be shown in the UI.                                                                |
| `waitFor`                   | boolean | `false`                                             | Whether to wait for the build to finish.                                                                          |
| `printBuildLogs`            | boolean | `false`                                             | Whether to print the build logs.                                                                                  |
| `buildEnvVars`              | String  |                                                     | Additional environment variables to set on the build, in KEY=VALUE format. No double quoting or extra `=`         |
| `artifactName`              | String  |                                                     | Artifact name                                                                                                     |
| `artifactPath`              | String  |                                                     | A file, directory or wildcard pattern that describes what to upload                                               |
| `artifactIfNoFilesFound`    | String  |                                                     | Passed to actons/upload-artifact. Equivalent to https://github.com/actions/upload-artifact/blob/v3/action.yml#L11 |

### outputs

| Name              | Type    | Description               |
|-------------------|---------| --------------------------|
| `build`           | String  |  The Buildkite build URL. |

## Limitations

### Transferring artifacts from Buildkite to GH Actions

Artifacts are uploaded in Buildkite then downloaded through the
[Buildkite Artifacts API](https://buildkite.com/docs/apis/rest-api/artifacts)
in the GH workflow and then uploaded to GH artifacts again with `actions/upload-artifact`.

When you specify `artifactsName` and `artifactsPath`, the files are not automatically uploaded
in a Buildkite pipeline. You must make sure that these files are uploaded in the targeted
Buildkite pipeline itself. This action onlyu downloads the files already uploaded in
Buildkite and uploads them again in GitHub via actions/upload-artifact.

Due to the functionality of the Buildkite Artifacts API, files are downloaded individually.
This means that if you use a glob that corresponds to 1000 files, 1000 requests will be sent to Buildkite.
To work around this problem, you can zip the files you want to transfer first and then upload the zip file.
The same problem is described in the actions/upload-artifact action itself in
https://github.com/actions/upload-artifact/tree/v3#too-many-uploads-resulting-in-429-responses.
