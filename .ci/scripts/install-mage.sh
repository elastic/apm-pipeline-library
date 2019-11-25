#!/usr/bin/env bash
set -exo pipefail

#
# Install the given go version using the gimme script.
#
# Parameters:
#   - GO_VERSION - that's the version which will be installed and enabled. v1.12.7 by default
#

GO_VERSION="1.12.7"

if [ -f ".go_version" ]; then
  GO_VERSION=$(cat .go_version)
fi

if [ -n "$1" ]; then
  GO_VERSION="${1}"
fi

# Install Go using the same travis approach
echo "Installing ${GO_VERSION} with gimme."
eval "$(curl -sL https://raw.githubusercontent.com/travis-ci/gimme/master/gimme | GIMME_GO_VERSION=${GO_VERSION} bash)"

# From https://magefile.org/#installation
go get -u -d github.com/magefile/mage
cd "${GOPATH}/src/github.com/magefile/mage"
go run bootstrap.go
