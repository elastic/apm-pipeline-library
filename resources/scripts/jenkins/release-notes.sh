#!/usr/bin/env bash
set -uxeo pipefail

GREN_GITHUB_TOKEN=${GREN_GITHUB_TOKEN:?"missing GREN_GITHUB_TOKEN"}

gren release --token="${GREN_GITHUB_TOKEN}" -c .grenrc.js --limit 10
# it is generated from scratch to have reverse version order
gren changelog --token="${GREN_GITHUB_TOKEN}" --generate --override -c .grenrc.js --changelog-filename=RELEASE_NOTES.md

# workaround to gerenare long Changelogs
# we have generated only a changelog for the latest version
# then we add it to the beginning of the CHANGELOG.md file.
mv CHANGELOG.md CHANGELOG.md.old
cat RELEASE_NOTES.md > CHANGELOG.md
grep -v "^# Changelog" CHANGELOG.md.old >> CHANGELOG.md
