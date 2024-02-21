## About

GitHub Action that waits for an artifact to be available on Maven central

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: release
on:
  workflow_dispatch:
    inputs:
      version:
        description: 'The version to release (e.g. 1.2.3). This workflow will automatically perform the required version bumps'
        required: true
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      ...

      - uses: elastic/apm-pipeline-library/.github/actions/await-maven-artifact@current
        with:
          groupid: "co.elastic.apm"
          artifactid: "apm-agent-java"
          version: "${{ inputs.version }}"
      ...
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name           | Type    | Default  | Description                         |
|----------------|---------|----------|-------------------------------------|
| `groupid`      | String  |          | Maven group-ID of the artifact.     |
| `artifactid`   | String  |          | Maven artifact-ID of the artifact.  |
| `version`      | String  |          | Version of the artifact to wait for.|
