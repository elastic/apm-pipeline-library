/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/**
  Execute some block of code if the built was trigger by a PR creation on the repo.
  It requires to initialise the pipeline with github_enterprise_constructor() first.
*/
void call(Map args = [:], body){
  
  // do nothing if not pr
  if (!env.GIT_BUILD_CAUSE.equals("pr")) 
    return
  
  def source_branch = env.BRANCH_NAME
  def target_branch = env.CHANGE_TARGET
    
  // do nothing in source branch doesn't match
  if (args.from)
  if (!(source_branch ==~ args.from))
    return

  // do nothing if target branch doesnt match
  if (args.to)
  if (!(target_branch ==~ args.to))
    return
  
  println "running because of a PR from ${source_branch} to ${target_branch}"
  body()
}
