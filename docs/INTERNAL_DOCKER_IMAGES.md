# Internal Docker Images Publish Process

To publish internal Docker images, we use a GitHub Actions workflow whose behavior is defined by a YAML configuration
file. You can find the workflow in [.github/workflows/publish-docker-images.yml](../.github/workflows/publish-docker-images.yml)
and the config in [.ci/.docker-images.yml](../.ci/.docker-images.yml).

## Add a new image

Add a new entry into the YAML [config](../.ci/.docker-images.yml) under the `images` key.

### Schema

| Key            | Type      | Description                                                                             | Required                             | Default  |
|----------------|-----------|-----------------------------------------------------------------------------------------|--------------------------------------|----------|
| name           | `string`  | The name of docker image in `<name>:<tag>`                                              | yes                                  | `null`   |
| tag            | `string`  | The tag of the docker image in `<name>:<tag>`                                           | no                                   | `latest` |
| repository     | `string`  | The GH repository name. E.g. elastic/apm-pipeline-library                               | no                                   | `null`   |
| build_opts     | `string`  | Extra build options for `docker build`. Ignored if you are using a custom build script. | no                                   | `null`   |
| prepare_script | `string`  | A script that runs before building.                                                     | no                                   | `null`   |
| build_script   | `string`  | Custom build script.                                                                    | no (yes, if push_script is defined)  | `null`   |
| push_script    | `string`  | Custom push script.                                                                     | no (yes, if build_script is defined) | `null`   |
| push           | `boolean` | Push the image.                                                                         | no                                   | `true`   |

### Example: New Entry

In the following example, the workflow will checkout the GitHub repository `elastic/apm-pipeline-library` and build the
image in the directory `.ci/docker/yamllint` and tag it with `docker.elastic.co/observability-ci/yamllint:latest`
and finally push it.

```yaml
images:
  - name: "yamllint"
    tag: "latest"
    repository: "elastic/apm-pipeline-library"
    working_dir: ".ci/docker/yamllint"
```

## Environment Variables

When you are using one of the following properties:
- `prepare_script`
- `build_script`
- `test_script`
- `push_script`

Then you can use the environment variables `REGISTRY`, `PREFIX`, `NAME` or `TAG` to.

| Env      | Description                            |
|----------|----------------------------------------|
| REGISTRY | Constant value: `docker.elastic.co`    |
| PREFIX   | Constant value: `observability-ci`     |
| NAME     | Value is based on the `name` property. |
| TAG      | Value is based on the `tag` property.  |

### Example: Environment Variables

In the following example the docker image `node:18-alpine` is pulled from docker.io.
Then it is retagged and pushed to the `docker.elastic.co` registry with the prefix `observability-ci`.

```yaml
  images:
    - name: "node"
      tag: "18-alpine"
      prepare_script: "docker pull ${NAME}:${TAG}"
      build_script: "docker tag ${NAME}:${TAG} ${REGISTRY}/${PREFIX}/${NAME}:${TAG}"
      push_script: "docker push ${REGISTRY}/${PREFIX}/${NAME}:${TAG}"
```

## Schema Validation

The config is automatically validated during a pre-commit hook and on a pull request
using a [JSON Schema](https://json-schema.org/), you can find the schema in
[.ci/.docker-images.schema.json](../.ci/.docker-images.schema.json).

> 💡 You can also set up your IDE to have live code inspection and autocompletion for a better experience.

## Pitfalls

Currently, only the schema of the config is validated. Although we use the option `"uniqueItems": true`, it cannot be
guaranteed that combinations of `name` and `tag` attributes can occur more than once.
Which means that multiple jobs may publish the same image. Especially when using custom build and push scripts,
there is currently no mechanism to prevent this behavior.
