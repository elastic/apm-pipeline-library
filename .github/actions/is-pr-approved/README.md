# is-pr-approved
[![License: Apache 2.0](https://img.shields.io/badge/license-Apache--2.0-yellow)](https://opensource.org/license/apache-2-0/)

*Check if we can execute a PR.*
* [Source](https://github.com/elastic/apm-pipeline-library)
* [Issues](https://github.com/elastic/apm-pipeline-library/issues)
* [Contact](mailto:adrien.mannocci@elastic.co)

## Prerequisites
* [NodeJS](https://nodejs.org/en) for development.

## Features
* Validate if a PR is approved to run within a workflow.
* Support default values extracted from context.
* Support Github PAT token.
* Support explicit allowed actors.

## Motivation & Context

* We needed a way to check if a PR is approved to run within a workflow.
* It enables the usage of `pull_request_target` event for forked repositories.
* The solution must allow a PR to run within the workflow if one of the following conditions is met:
  * The workflow has been triggered by an allowed actor (explicit list of actors).
  * The workflow has been triggered by an actor with `write/admin` permissions on the repository.
  * The workflow has been triggered by an org actor.
  * The PR has been approved (review) by a trusted actor.

## Workflow

### Setup
The following steps will ensure your project is cloned properly.
1. `npm i`

### Lint
* To lint you have to use the workflow.

```bash
npm run lint
```

* It will lint the project code using `eslint`.

### Format
* To format you have to use the workflow.

```bash
npm run fmt
```

* It will format the project code using `prettier`.

### Build
* To build you have to use the workflow.

```bash
npm run build
```

* It will test the project code using `tsc`.

### Test
* To test you have to use the workflow.

```bash
npm test
```

* It will test the project code using `jest`.

### Package
* To test you have to use the workflow.

```bash
npm run package
```

* It will package the project code using `ncc`.

## Usage

### How it works

* The action will check if the trigger event is either `pull_request` or `pull_request_target`.
* It will extract the GitHub token from context.
* Check for known actors.
* Check for actor permission of the repository.
* Check for actor membership of the org.
* Check for an approved review on the PR.

### How to use it

```yaml
---
name: Is PR Approved

on:
  # One of the following event
  pull_request:
  pull_request_target:
  push:
  workflow_dispatch:

permissions:
  contents: read

jobs:
  is-pr-approved:
    runs-on: ubuntu-latest
    permissions:
      contents: 'read'
      # Needed to validate PR using reviews
      pull-requests: 'read'
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/is-pr-approved@current
        with:
          # Define a specific PAT token (optional)
          github-token: 'PAT Token'
          # Define a list of allowed actors delimited by ',' (optional)
          # Default: greenkeeper[bot], dependabot[bot], mergify[bot], github-actions[bot], renovate[bot]
          allowed-actors: 'octokit,octocat'
      - run: echo 'The PR is approved'

  run-safely:
    runs-on: ubuntu-latest
    needs:
      - is-pr-approved
    steps:
      - run: echo 'The PR is approved'
```

## Contributing

If you find this project useful here's how you can help :

* Send a Pull Request with your awesome new features and bug fixed
* Be a part of the community and help resolve [Issues](https://github.com/elastic/apm-pipeline-library/issues)
