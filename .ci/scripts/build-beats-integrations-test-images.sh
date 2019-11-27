#!/usr/bin/env bash
set -exo pipefail

#
# Install the given go version using the gimme script.
#
# Parameters:
#   - GO_VERSION - that's the version which will be installed and enabled.
#

readonly GO_VERSION="${1?Please define the Go version to be used}"

function install_go() {
    local goVersion="${1}"

    # Install Go using the same travis approach
    echo "Installing ${goVersion} with gimme."
    eval "$(curl -sL https://raw.githubusercontent.com/travis-ci/gimme/master/gimme | GIMME_GO_VERSION=${goVersion} bash)"
}

function main() {
    install_go "${GO_VERSION}"
}

main "$@"
