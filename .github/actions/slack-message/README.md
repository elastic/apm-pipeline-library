# slack-message
[![License: Apache 2.0](https://img.shields.io/badge/license-Apache--2.0-yellow)](https://opensource.org/license/apache-2-0/)

*Check if we can execute a PR.*
* [Source](https://github.com/elastic/apm-pipeline-library)
* [Issues](https://github.com/elastic/apm-pipeline-library/issues)
* [Contact](mailto:observability-robots@elastic.co)

## Prerequisites
* [NodeJS](https://nodejs.org/en) for development.

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

### How to use it

```yaml
---
name: Send Slack Message

on:
  pull_request:
  push:
  workflow_dispatch:

permissions:
  contents: read

jobs:
  send-slack-message:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/slack-message@current
        with:
          # Define a specific vault url (required)
          url: ''
          # Define a specific vault role id (required)
          roleId: ''
          # Define a specific vault secret id (required)
          secretId: ''
          # Define a specific slack channel (required)
          channel: ''
          # Mask generate payload in logs (optional: bool)
          mask: ''

          # Define either the message or payload input
          # Define the message to send using markdown format
          message: ''
          # Define the payload in json format
          # Ref: https://github.com/slackapi/slack-github-action#usage-1
          payload: ''
```

## Contributing

If you find this project useful here's how you can help :

* Send a Pull Request with your awesome new features and bug fixed
* Be a part of the community and help resolve [Issues](https://github.com/elastic/apm-pipeline-library/issues)
