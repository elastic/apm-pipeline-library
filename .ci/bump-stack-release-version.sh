#!/usr/bin/env bash
#
# Given the stack version this script will bump the release version.
#
# This script is executed by the automation we are putting in place
# and it requires the git add/commit commands.
#
# Parameters:
#	$1 -> the minor version to be bumped. Mandatory.
#	$1 -> the major version to be bumped. Mandatory.
#
set -euo pipefail
MSG="parameter missing."
VERSION_RELEASE=${1:?$MSG}
VERSION_DEV=${2:?$MSG}

OS=$(uname -s| tr '[:upper:]' '[:lower:]')

if [ "${OS}" == "darwin" ] ; then
	SED="sed -i .bck"
else
	SED="sed -i"
fi

echo "Update stack with versions ${VERSION_RELEASE} and ${VERSION_DEV}"
${SED} -E -e "s#(def releaseVersion) = \"[0-9]+\.[0-9]+\.[0-9]\"#\1 = \"${VERSION_RELEASE}\"#g" vars/stackVersions.groovy
${SED} -E -e "s#(def devVersion) = \"[0-9]+\.[0-9]+\.[0-9]\"#\1 = \"${VERSION_DEV}\"#g" vars/stackVersions.groovy
${SED} -E -e "s#(docker.elastic.co/beats/.*beat:)[0-9]+\.[0-9]+\.[0-9]#\1${VERSION_DEV}#g" .ci/packer_cache.sh
${SED} -E -e "s#(docker.elastic.co/beats/.*beat:)[0-9]+\.[0-9]+\.[0-9]#\1${VERSION_DEV}#g" vars/filebeat.groovy
${SED} -E -e "s#(docker.elastic.co/beats/.*beat:)[0-9]+\.[0-9]+\.[0-9]#\1${VERSION_DEV}#g" vars/metricbeat.groovy

git add vars/stackVersions.groovy
git add .ci/packer_cache.sh
git add vars/filebeat.groovy
git add vars/metricbeat.groovy
git diff --staged --quiet || git commit -m "[Automation] Update elastic stack release version to ${VERSION_RELEASE} and ${VERSION_DEV}"
git --no-pager log -1

echo "You can now push and create a Pull Request"
