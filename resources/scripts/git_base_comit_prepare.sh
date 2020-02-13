#!/usr/bin/env bash
set -uxeo pipefail

# checkout the repository
git clone https://github.com/v1v/its-gitbase
cd its-gitbase

# create a new branch and make a file change
# then make a PR to the masters
git checkout origin/master -b git_base_commit_1_commit
echo $RANDOM > file-change-0
git add file-change-0
git commit -a -m "test: file change"
git push origin git_base_commit_1_commit
hub pull-request --labels automation -m "test: PR behind"

# checkout the master branch and make a file change
git checkout master
echo $RANDOM > file-change
git add file-change
git commit -a -m "test: file change"
git push origin master

# create a new branch and make a file change
# then make a PR to the masters
git checkout origin/master -b git_base_commit_0_commit
echo $RANDOM > file-change-1
git add file-change-1
git commit -a -m "test: file change"
git push origin git_base_commit_0_commit
hub pull-request --labels automation -m "test: PR ahead"
