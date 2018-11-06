/**
  Delete a git TAG named ${BUILD_TAG} and push it to the git repo.
  It requires to initialise the pipeline with github_enterprise_constructor() first.

  gitDeleteTag()
*/

def call() {
  dir('cleanTags'){
    unstash 'source'
    sh("""
    git fetch --tags
    git tag -d '${BUILD_TAG}'
    git push git@github.com:${ORG_NAME}/${REPO_NAME}.git --tags
    """)
    deleteDir()
  }
}