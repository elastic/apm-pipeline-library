# Docker images

List of docker images that are used in different pipelines

## How to use it

```bash

# Build and test docker images
$ make all-tests

# Push docker images to the docker registry
$ make all-push

## Folders

### build

This is the docker image for building/testing this particular project

It is consumed by the k8s plugin
