#!/usr/bin/env bash
set -eo pipefail

# From https://magefile.org/#installation
go get -u -d github.com/magefile/mage
cd "${GOPATH}/src/github.com/magefile/mage"
go run bootstrap.go
