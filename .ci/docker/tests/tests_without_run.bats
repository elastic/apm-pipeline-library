#!/usr/bin/env bats

load 'test_helper/bats-support/load'
load 'test_helper/bats-assert/load'
load test_helpers

IMAGE="docker.elastic.co/observability-ci/${DOCKERFILE//\//-}"
CONTAINER="${DOCKERFILE//\//-}"

@test "${DOCKERFILE} - build image" {
	cd $BATS_TEST_DIRNAME/..
	# Simplify the makefile as it does fail with '/bin/sh: 1: Bad substitution' in the CI
	if [ ! -e ${DOCKERFILE} ] ; then
		DOCKERFILE="${DOCKERFILE//-//}"
	fi
	run docker build --rm -t ${IMAGE} ${DOCKERFILE}
	assert_success
}

@test "${DOCKERFILE} - clean test containers" {
	cleanup $CONTAINER
}
