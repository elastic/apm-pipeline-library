#!/usr/bin/env bash
set -exo pipefail

#
# Install the given go version using the gimme script.
#
# Parameters:
#   - GO_VERSION - that's the version which will be installed and enabled.
#

readonly GO_VERSION="${1?Please define the Go version to be used}"

# Install Go using the same travis approach
echo "Installing ${GO_VERSION} with gimme."
eval "$(curl -sL https://raw.githubusercontent.com/travis-ci/gimme/master/gimme | GIMME_GO_VERSION=${GO_VERSION} bash)"
