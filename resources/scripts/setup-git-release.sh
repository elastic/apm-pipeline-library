#!/usr/bin/env bash
set -exo pipefail

# Prepare the git workspace for the release in the CI
# This is caused by the detached default repository.
#
# Some environment variables are required to be exposed beforehand:
# - BRANCH_NAME, the branch name. This is set on the fly when using the Multibranch Pipeline.
# - GIT_BASE_COMMIT, the sha commit. This is set on the fly when using the gitCheckout step.
# - GITHUB_TOKEN , the GitHub api token. This is set on the fly with some credentials.
# - GITHUB_USER, the GitHub user account. This is set on the fly with some credentials.
# - ORG_NAME, the GitHub organisation. This is set on the fly when using the gitCheckout step.
# - REPO_NAME, the GitHub repo. This is set on the fly when using the gitCheckout step.
#

# Validate the env variables have been configured properly
BRANCH_NAME=${BRANCH_NAME:?"env variable is missing"}
GIT_BASE_COMMIT=${GIT_BASE_COMMIT:?"env variable is missing"}
GITHUB_TOKEN=${GITHUB_TOKEN:?"env variable is missing"}
GITHUB_USER=${GITHUB_USER:?"env variable is missing"}
ORG_NAME=${ORG_NAME:?"env variable is missing"}
REPO_NAME=${REPO_NAME:?"env variable is missing"}

# Enable git+https. Env variables are created on the fly with the gitCheckout
git config remote.origin.url "https://${GITHUB_USER}:${GITHUB_TOKEN}@github.com/${ORG_NAME}/${REPO_NAME}.git"

# Enable to fetch branches when cloning with a detached and shallow clone
git config remote.origin.fetch '+refs/heads/*:refs/remotes/origin/*'

# Force the git user details when pushing using the last commit details
USER_MAIL=$(git log -1 --pretty=format:'%ae')
USER_NAME=$(git log -1 --pretty=format:'%an')
git config user.email "${USER_MAIL}"
git config user.name "${USER_NAME}"

# Checkout the branch as it's detached based by default.
# See https://issues.jenkins-ci.org/browse/JENKINS-33171
git fetch --all
git checkout "${BRANCH_NAME}"

# Enable upstream with git+https.
git remote add upstream "https://${GITHUB_USER}:${GITHUB_TOKEN}@github.com/${ORG_NAME}/${REPO_NAME}.git"
git fetch --all

# Pull the git history when repo was cloned with shallow/depth.
if [ -f "$(git rev-parse --git-dir)/shallow" ] || [ "$(git rev-parse --is-shallow-repository)" = "true" ]; then
    git pull --unshallow
else
    git pull
fi

# Ensure the branch points to the original commit to avoid commit injection
# when running the release pipeline.
# used GIT_BASE_COMMIT instead GIT_COMMIT to support the MultiBranchPipelines.
git reset --hard "${GIT_BASE_COMMIT}"
