#!/usr/bin/env bash
set -exo pipefail

#
# Install the given go version using the gimme script.
#
# Parameters:
#   - METRICBEAT_MODULE - that's the name of the metricbeat module to be released.
#   - GO_VERSION - that's the version which will be installed and enabled.
#

readonly METRICBEAT_MODULE="${1?Please set the metricbeat module to be released}"
readonly GO_VERSION="${2?Please define the Go version to be used}"

# Install Go using the same travis approach
echo "Installing ${GO_VERSION} with gimme."
eval "$(curl -sL https://raw.githubusercontent.com/travis-ci/gimme/master/gimme | GIMME_GO_VERSION=${GO_VERSION} bash)"

# From https://magefile.org/#installation
go get -u -d github.com/magefile/mage
cd "${GOPATH}/src/github.com/magefile/mage"
go run bootstrap.go

MODULE="${METRICBEAT_MODULE}" mage compose:buildSupportedVersions
MODULE="${METRICBEAT_MODULE}" mage compose:pushSupportedVersions
