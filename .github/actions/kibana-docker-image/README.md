## About

This action builds and pushes a Kibana docker image to a registry.
Afterward, it outputs the docker image reference, which can be used in other steps.
___

## Example

```yaml
---
name: example

on: workflow_dispatch

jobs:
  kibana-docker-image-cloud:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: elastic/apm-pipeline-library/.github/actions/docker-login@current
        with:
          registry: docker.elastic.co
          secret: secret/observability-team/ci/docker-registry/prod
          url: ${{ secrets.VAULT_ADDR }}
          roleId: ${{ secrets.VAULT_ROLE_ID }}
          secretId: ${{ secrets.VAULT_SECRET_ID }}
      - uses: elastic/apm-pipeline-library/.github/actions/kibana-docker-image@current
        id: kibana-docker-image
        with:
          git-ref: main # git ref of elastic/kibana
          serverless: true # Default: false
      - run: |
          echo "${DOCKER_IMAGE:?}"
          docker pull "${DOCKER_IMAGE}"
        env:
          DOCKER_IMAGE: ${{ steps.kibana-docker-image.outputs.ref }}


```

## Inputs

Following inputs can be used as `step.with` keys

| Name                 | Type   | Required | Description                                                                   |
|----------------------|--------|----------|-------------------------------------------------------------------------------|
| `github-repository`  | String | no       | The git repository to checkout. (Default: `elastic/kibana`)                   |
| `git-ref`            | String | no       | The git ref of the repository. (Default: default branch, e.g. `main`)         |
| `serverless`         | String | no       | Whether to build serverless images or not. (Default: `false`)                 |
| `docker-registry`    | String | no       | The docker registry for pushing the image. (Default: `docker.elastic.co`)     |
| `docker-namespace`   | String | no       | The namespace of the repository. (Default: `observability-ci`)                |
| `checkout-path`      | String | no       | The path to checkout the git repository to. (Default: `kibana-repo-checkout`) |

## Outputs

| Name   | Description                                         |
|--------|-----------------------------------------------------|
| `ref`  | The published docker image reference. (`image:tag`) |
