# Changelog

## v1.1.65 (03/02/2020)

#### 🚀 Enhancements

-  feat: generate TOTP code with Vault and .npmrc [#367](https://github.com/elastic/apm-pipeline-library/pull/367)
- [**groovy**][**master**] feat: git step support sleep/retry [#370](https://github.com/elastic/apm-pipeline-library/pull/370)

#### 🐛 Bug Fixes

-  fix: support gitCheckout without a previous git repo [#371](https://github.com/elastic/apm-pipeline-library/pull/371)

#### 📚 Documentation

- [**groovy**] add more test coverage for the gitCheckout step [#374](https://github.com/elastic/apm-pipeline-library/pull/374)

#### ⚙️ CI

-  refactor: enum for the secrets [#368](https://github.com/elastic/apm-pipeline-library/pull/368)

---

## v1.1.64 (28/01/2020)

#### 🐛 Bug Fixes

- [**groovy**] fixes: build step with wait:false [#364](https://github.com/elastic/apm-pipeline-library/pull/364)
-  fixes: WorkflowScript: 145: The current scope already contains a variable [#363](https://github.com/elastic/apm-pipeline-library/pull/363)

#### ⚙️ CI

-  [cache] Build ruby and python docker images [#362](https://github.com/elastic/apm-pipeline-library/pull/362)
-  feat: build opbot daily [#361](https://github.com/elastic/apm-pipeline-library/pull/361)

---

## v1.1.63 (24/01/2020)

#### 🐛 Bug Fixes

- [**groovy**] fixes the foo hardcoded string for testing purposes [#360](https://github.com/elastic/apm-pipeline-library/pull/360)

---

## v1.1.62 (24/01/2020)

#### 🐛 Bug Fixes

- [**groovy**] fixes the isCommentTrigger step when orgs for the user are not available [#359](https://github.com/elastic/apm-pipeline-library/pull/359)

#### 📚 Documentation

-  Update docs with some dependencies when running from scratch [#354](https://github.com/elastic/apm-pipeline-library/pull/354)

---

## v1.1.61 (20/01/2020)

#### 🚀 Enhancements

-  ci(jenkins): override the built-in checkout step [#352](https://github.com/elastic/apm-pipeline-library/pull/352)

---

## v1.1.60 (17/01/2020)

#### 🚀 Enhancements

-  ci(jenkins): workaround the timeout with a sleep [#351](https://github.com/elastic/apm-pipeline-library/pull/351)

---

## v1.1.59 (16/01/2020)
*No changelog for this release.*

---

## v1.1.58 (16/01/2020)

#### 🚀 Enhancements

-  ci(jenkins): enable tag args and force the tag creation [#348](https://github.com/elastic/apm-pipeline-library/pull/348)

#### 🐛 Bug Fixes

-  fix: null string in the debug log [#349](https://github.com/elastic/apm-pipeline-library/pull/349)

#### 📚 Documentation

-  Add ssh-agent explicitly [#346](https://github.com/elastic/apm-pipeline-library/pull/346)

#### ⚙️ CI

-  test: script to create the git_base_commit PRs [#345](https://github.com/elastic/apm-pipeline-library/pull/345)

---

## v1.1.57 (16/01/2020)

#### 🐛 Bug Fixes

-  override customised env variables and fix fetch prs references [#347](https://github.com/elastic/apm-pipeline-library/pull/347)

---

## v1.1.56 (16/01/2020)

#### 🚀 Enhancements

-  notify downstream build failure and rebuild if downstream timeouts [#342](https://github.com/elastic/apm-pipeline-library/pull/342)
-  fix: add ubuntu label and windows-immutable label [#343](https://github.com/elastic/apm-pipeline-library/pull/343)

#### 🐛 Bug Fixes

-  Re-implement GIT_BASE_COMIT calculation [#339](https://github.com/elastic/apm-pipeline-library/pull/339)
-  fix: failed builds don't return a RunWrapper object [#341](https://github.com/elastic/apm-pipeline-library/pull/341)

---

## v1.1.55 (13/01/2020)

#### 🐛 Bug Fixes

-  fix: support docker.inside with the precommit step [#338](https://github.com/elastic/apm-pipeline-library/pull/338)

---

## v1.1.54 (10/01/2020)

#### 🚀 Enhancements

-  Support the rebuild for some other MBPs [#334](https://github.com/elastic/apm-pipeline-library/pull/334)

#### 🐛 Bug Fixes

-  Use absoluteUrl method when generating the URL for the build steps [#335](https://github.com/elastic/apm-pipeline-library/pull/335)
-  Skip null string when GitHub timeouts [#336](https://github.com/elastic/apm-pipeline-library/pull/336)

---

## v1.1.53 (09/01/2020)

#### 🚀 Enhancements

-  Add options parser to test-jjbb [#323](https://github.com/elastic/apm-pipeline-library/pull/323)

#### 🐛 Bug Fixes

-  (pre-commit) avoid error when pulling docker images [#331](https://github.com/elastic/apm-pipeline-library/pull/331)
-  ci(jenkins): validate pipelines [#326](https://github.com/elastic/apm-pipeline-library/pull/326)

#### ⚙️ CI

-  ci: bump version of the local jenkins instance [#327](https://github.com/elastic/apm-pipeline-library/pull/327)

---

## v1.1.52 (07/01/2020)

#### 🚀 Enhancements

-  Reuse GitHub comment when possible [#324](https://github.com/elastic/apm-pipeline-library/pull/324)
-  enable HOME/PATH env variables for the preCommit's step context [#325](https://github.com/elastic/apm-pipeline-library/pull/325)
-  Add information on Docker creds [#317](https://github.com/elastic/apm-pipeline-library/pull/317)

#### ⚙️ CI

-  refactor: use the ApmBasePipelineTest superclass [#319](https://github.com/elastic/apm-pipeline-library/pull/319)
-  ci(jenkins): support rebuild for the toplevel apm-integration-tests pipeline [#318](https://github.com/elastic/apm-pipeline-library/pull/318)

---

## v1.1.51 (18/12/2019)

#### 🚀 Enhancements

-  support upstreamTrigger validation in the 1st time contributors [#315](https://github.com/elastic/apm-pipeline-library/pull/315)

#### 🐛 Bug Fixes

-  revert isCommentTrigger implementation [#316](https://github.com/elastic/apm-pipeline-library/pull/316)

---

## v1.1.50 (18/12/2019)

#### ⚙️ CI

-  enable rebuild for the other main MPBs [#314](https://github.com/elastic/apm-pipeline-library/pull/314)

---

## v1.1.49 (17/12/2019)

#### 🚀 Enhancements

-  enable rebuild if checkout issues [#311](https://github.com/elastic/apm-pipeline-library/pull/311)

#### ⚙️ CI

-  ci(jenkins): enable github pr comments [#309](https://github.com/elastic/apm-pipeline-library/pull/309)

---

## v1.1.48 (16/12/2019)

#### 🚀 Enhancements

-  enable github comment for pull requests [#308](https://github.com/elastic/apm-pipeline-library/pull/308)
-  Enable to customise the GH checks with URLs [#307](https://github.com/elastic/apm-pipeline-library/pull/307)

---

## v1.1.47 (13/12/2019)

#### ⚙️ CI

-  retry when gitCheckout as a workaround when timeouts [#306](https://github.com/elastic/apm-pipeline-library/pull/306)

---

## v1.1.46 (10/12/2019)

#### 🐛 Bug Fixes

-  ci(jenkins): support gitCheckout with customisation [#303](https://github.com/elastic/apm-pipeline-library/pull/303)
-  fix: use 'origin/BRANCH' only on PRs [#302](https://github.com/elastic/apm-pipeline-library/pull/302)

#### ⚙️ CI

-  ci(jjbb): only PRs for the e2e Kibana UI [#301](https://github.com/elastic/apm-pipeline-library/pull/301)

---

## v1.1.45 (29/11/2019)

#### 🚀 Enhancements

-  feat: pipelineManager to encapsulate certain steps [#300](https://github.com/elastic/apm-pipeline-library/pull/300)
-  feat: setEnvVar, whenTrue, whenFalse, and withEnvMask new steps [#295](https://github.com/elastic/apm-pipeline-library/pull/295)

#### 🐛 Bug Fixes

-  fix: use GIT_PREVIOUS_COMMIT on isGitRegionMatch step [#298](https://github.com/elastic/apm-pipeline-library/pull/298)

---

## v1.1.44 (28/11/2019)

#### 🚀 Enhancements

-  support abort ongoing old builds [#294](https://github.com/elastic/apm-pipeline-library/pull/294)

---

## v1.1.43 (27/11/2019)

#### 🚀 Enhancements

-  release process for opbeans [#271](https://github.com/elastic/apm-pipeline-library/pull/271)
-  fix: avoid NPE on inconsistent return objects [#279](https://github.com/elastic/apm-pipeline-library/pull/279)
-  test: set permissions over a folder [#284](https://github.com/elastic/apm-pipeline-library/pull/284)
-  test: set access permissions from the Jenkinsfile [#282](https://github.com/elastic/apm-pipeline-library/pull/282)
-  [pre-commit] Default apm-ci jenkins instance and validate ./Jenkinsfile [#281](https://github.com/elastic/apm-pipeline-library/pull/281)
-  feat: use no blacklisted methods [#275](https://github.com/elastic/apm-pipeline-library/pull/275)

#### 🐛 Bug Fixes

-  fix: avoid lose Exceptions [#291](https://github.com/elastic/apm-pipeline-library/pull/291)
-  (#213) Fix script path [#290](https://github.com/elastic/apm-pipeline-library/pull/290)
-  213 fix gopath [#289](https://github.com/elastic/apm-pipeline-library/pull/289)
-  (#213) Define HOME variable for ephemeral worker installing Gimme [#288](https://github.com/elastic/apm-pipeline-library/pull/288)
-  (#213) Install mage in the build agent [#276](https://github.com/elastic/apm-pipeline-library/pull/276)

#### 📚 Documentation

-  refactor: simplify asserts with some helper functions [#293](https://github.com/elastic/apm-pipeline-library/pull/293)
-  (#272) Add system requirements for local compose [#273](https://github.com/elastic/apm-pipeline-library/pull/273)

#### ⚙️ CI

-  (#213) Build Metricbeat's test Docker images in a daily manner [#274](https://github.com/elastic/apm-pipeline-library/pull/274)

---

## v1.1.42 (20/11/2019)

#### 🚀 Enhancements

-  feat: notify to the distribution list with the filter [#269](https://github.com/elastic/apm-pipeline-library/pull/269)
-  feat: cluster management with the repo itself [#266](https://github.com/elastic/apm-pipeline-library/pull/266)

#### 📚 Documentation

-  [docs] REPO env variable design pattern [#270](https://github.com/elastic/apm-pipeline-library/pull/270)
-  design: add comment with the how to use the pipeline [#267](https://github.com/elastic/apm-pipeline-library/pull/267)

#### ⚙️ CI

-  [jjbb] check_paths_for_matches.py not required anymore [#268](https://github.com/elastic/apm-pipeline-library/pull/268)

---

## v1.1.41 (18/11/2019)

#### 🐛 Bug Fixes

-  fixes isGitRegionMatch to be used only with gitCheckout and minor changes [#264](https://github.com/elastic/apm-pipeline-library/pull/264)
-  fix grep per line rather than the whole content [#260](https://github.com/elastic/apm-pipeline-library/pull/260)

#### ⚙️ CI

-  Move pipelines to the obs-test-environments repo [#262](https://github.com/elastic/apm-pipeline-library/pull/262)

---

## v1.1.40 (15/11/2019)

#### 🚀 Enhancements

-  bump version from 2.x support to 3.x support in order to use the filter-branch [#256](https://github.com/elastic/apm-pipeline-library/pull/256)
-  feat: PR GitHub template [#254](https://github.com/elastic/apm-pipeline-library/pull/254)
-  Bump jenkins core version for local testing [#247](https://github.com/elastic/apm-pipeline-library/pull/247)
-  enforce: maven dependencies and validation within the build [#246](https://github.com/elastic/apm-pipeline-library/pull/246)
-  Configure missing credentials [#244](https://github.com/elastic/apm-pipeline-library/pull/244)
-  override build step to print the downstream URL [#259](https://github.com/elastic/apm-pipeline-library/pull/259)
-  support exact match in isGitRegionMatch [#257](https://github.com/elastic/apm-pipeline-library/pull/257)

#### 🐛 Bug Fixes

-  CRUMB issues when running the validate.sh [#241](https://github.com/elastic/apm-pipeline-library/pull/241)

#### 📚 Documentation

-  Build rotation design [#258](https://github.com/elastic/apm-pipeline-library/pull/258)

#### ⚙️ CI

-  Revert "fix: use fork to build the images" [#255](https://github.com/elastic/apm-pipeline-library/pull/255)
-  fix: use fork to build the images [#253](https://github.com/elastic/apm-pipeline-library/pull/253)
-  ci: bring up to date the 'update clusters' job [#243](https://github.com/elastic/apm-pipeline-library/pull/243)

---

## v1.1.39 (30/09/2019)

#### 🚀 Enhancements

-  enable curl with timeout and connection timeout [#239](https://github.com/elastic/apm-pipeline-library/pull/239)
-  feat: use infra docker image and simplify the list of plugins [#238](https://github.com/elastic/apm-pipeline-library/pull/238)
-  feat: enable local windows workers [#237](https://github.com/elastic/apm-pipeline-library/pull/237)

---

## v1.1.38 (25/09/2019)

#### 🚀 Enhancements

-  support downstream builds for the opbeans pipeline step [#234](https://github.com/elastic/apm-pipeline-library/pull/234)

#### 🐛 Bug Fixes

-  fix field in the declarative pipeline [#236](https://github.com/elastic/apm-pipeline-library/pull/236)
-  minor fixes [#235](https://github.com/elastic/apm-pipeline-library/pull/235)

---

## v1.1.37 (24/09/2019)

#### 🚀 Enhancements

-  enable macosx vagrant box for testing purposes [#233](https://github.com/elastic/apm-pipeline-library/pull/233)

#### 🙈 No user affected

-  Refactor test classes [#232](https://github.com/elastic/apm-pipeline-library/pull/232)

#### ⚙️ CI

-  enable opbeans pipeline [#230](https://github.com/elastic/apm-pipeline-library/pull/230)

---

## v1.1.36 (19/09/2019)

#### 🚀 Enhancements

-  enable labels when connecting the swarm linux agent [#229](https://github.com/elastic/apm-pipeline-library/pull/229)
-  rubygemsLogin step [#227](https://github.com/elastic/apm-pipeline-library/pull/227)

#### 🐛 Bug Fixes

-  fix: regex in the tag pattern [#228](https://github.com/elastic/apm-pipeline-library/pull/228)

---

## v1.1.35 (17/09/2019)

#### 🚀 Enhancements

-  feat: enable linux worker [#223](https://github.com/elastic/apm-pipeline-library/pull/223)
-  feat: enable manual ITs [#204](https://github.com/elastic/apm-pipeline-library/pull/204)
-  enable jjbb validation locally [#217](https://github.com/elastic/apm-pipeline-library/pull/217)
-  force shallow to False if mergeTarget is enabled [#220](https://github.com/elastic/apm-pipeline-library/pull/220)
-  feat: CodeCov Docker container as tool [#172](https://github.com/elastic/apm-pipeline-library/pull/172)
-  feat: support multidocument YAML files [#210](https://github.com/elastic/apm-pipeline-library/pull/210)

#### 🐛 Bug Fixes

-  fix: replace only the first '-' with a ':' [#211](https://github.com/elastic/apm-pipeline-library/pull/211)
-  docker pull --quiet is not supported by default [#215](https://github.com/elastic/apm-pipeline-library/pull/215)
-  fix: update workers stages [#209](https://github.com/elastic/apm-pipeline-library/pull/209)

#### 🙈 No user affected

-  refactor ITs layout [#221](https://github.com/elastic/apm-pipeline-library/pull/221)

#### ⚙️ CI

-  Use the new credentials for the benchmark ES stack [#226](https://github.com/elastic/apm-pipeline-library/pull/226)
-  ci(jenkins): update opbeans Docker images build job [#224](https://github.com/elastic/apm-pipeline-library/pull/224)

---

## v1.1.34 (03/09/2019)

#### 🚀 Enhancements

-  feat: test cluster for 7.4.0 [#205](https://github.com/elastic/apm-pipeline-library/pull/205)
-  docker pull quietly to reduce log verbose output [#203](https://github.com/elastic/apm-pipeline-library/pull/203)

#### 🐛 Bug Fixes

-  fix: shallow cloning with mergeTarget is not allowed [#196](https://github.com/elastic/apm-pipeline-library/pull/196)
-  fix: net.sf.json.JSONNull when authentication is disabled [#207](https://github.com/elastic/apm-pipeline-library/pull/207)
-  fix: build version might differ when using aliases [#208](https://github.com/elastic/apm-pipeline-library/pull/208)

---

## v1.1.33 (23/08/2019)

#### 🚀 Enhancements

-  add more details to the error when gitCheckout [#190](https://github.com/elastic/apm-pipeline-library/pull/190)

#### 🐛 Bug Fixes

-  HOME is required to be declared when using the preCommit [#193](https://github.com/elastic/apm-pipeline-library/pull/193)

#### 📚 Documentation

- [**question**] fix: checkGitChanges is not used and no UTs [#195](https://github.com/elastic/apm-pipeline-library/pull/195)

#### 🙈 No user affected

-  refactor mock classes and interceptors [#192](https://github.com/elastic/apm-pipeline-library/pull/192)
-  Revert "HOME is required to be declared when using the preCommit" [#194](https://github.com/elastic/apm-pipeline-library/pull/194)
-  jjbb: without the branch parameter [#191](https://github.com/elastic/apm-pipeline-library/pull/191)

---

## v1.1.32 (13/08/2019)

#### 🚀 Enhancements

-  feat: support docker image when running preCommit [#188](https://github.com/elastic/apm-pipeline-library/pull/188)
-  feat: vault is not required in the jjbb [#187](https://github.com/elastic/apm-pipeline-library/pull/187)
-  feat: enable functional tests pipeline [#186](https://github.com/elastic/apm-pipeline-library/pull/186)
-  feat: rename GitHub check context as apm-ci [#184](https://github.com/elastic/apm-pipeline-library/pull/184)
-  support withVaultToken step [#183](https://github.com/elastic/apm-pipeline-library/pull/183)
-  dockerLogin for the precommit wrapper [#182](https://github.com/elastic/apm-pipeline-library/pull/182)
-  feat: Build Apm Server Docker images [#150](https://github.com/elastic/apm-pipeline-library/pull/150)
-  use orgs API for checking if build was triggered by a comment [#178](https://github.com/elastic/apm-pipeline-library/pull/178)

#### 🐛 Bug Fixes

-  fix: PATH env variable is required in the inside method to be expanded [#189](https://github.com/elastic/apm-pipeline-library/pull/189)

#### 🙈 No user affected

-  fix: build all the PRs and all the branches without any PRs [#181](https://github.com/elastic/apm-pipeline-library/pull/181)

---

## v1.1.31 (08/08/2019)

#### 🚀 Enhancements

-  feat: enable gherkin-lint pre-commit hook [#177](https://github.com/elastic/apm-pipeline-library/pull/177)
-  feat: pre-commit hooks library [#168](https://github.com/elastic/apm-pipeline-library/pull/168)
-  add some debug logs to the isGitRegionMatch  [#176](https://github.com/elastic/apm-pipeline-library/pull/176)

#### 🐛 Bug Fixes

-  withEnv in the checkLicenses step [#175](https://github.com/elastic/apm-pipeline-library/pull/175)
-  fix: trim for trailing spaces when comparing the company  [#174](https://github.com/elastic/apm-pipeline-library/pull/174)

---

## v1.1.30 (07/08/2019)

#### 🚀 Enhancements

-  fix: support traditional views as BO returns 404 with crumb [#173](https://github.com/elastic/apm-pipeline-library/pull/173)

---

## v1.1.29 (02/08/2019)

#### 🚀 Enhancements

-  feat: support cloneOptions by default [#170](https://github.com/elastic/apm-pipeline-library/pull/170)

#### 🐛 Bug Fixes

-  fix appended protocol [#169](https://github.com/elastic/apm-pipeline-library/pull/169)

#### 🙈 No user affected

-  pre-commit more test coverage [#171](https://github.com/elastic/apm-pipeline-library/pull/171)

---

## v1.1.28 (31/07/2019)

#### 🚀 Enhancements

-  feat: enable jjbb validation within the pre-commit [#163](https://github.com/elastic/apm-pipeline-library/pull/163)
-  feat: prepareAndRun for the benchmark [#167](https://github.com/elastic/apm-pipeline-library/pull/167)
-  ci: enable PoC for the metricbeats [#148](https://github.com/elastic/apm-pipeline-library/pull/148)
-  feat: use preCommit step [#166](https://github.com/elastic/apm-pipeline-library/pull/166)

#### 🐛 Bug Fixes

-  fix: force git push [#164](https://github.com/elastic/apm-pipeline-library/pull/164)

#### ⚙️ CI

-  feat: remove submodules [#149](https://github.com/elastic/apm-pipeline-library/pull/149)

---

## v1.1.27 (29/07/2019)

#### 🚀 Enhancements

-  windows is not supported in some steps [#162](https://github.com/elastic/apm-pipeline-library/pull/162)
-  feat: enable precommit step [#158](https://github.com/elastic/apm-pipeline-library/pull/158)

#### 🙈 No user affected

-  Rename test methods [#161](https://github.com/elastic/apm-pipeline-library/pull/161)

#### ⚙️ CI

-  fix: create and delete tag steps [#160](https://github.com/elastic/apm-pipeline-library/pull/160)

---

## v1.1.26 (26/07/2019)
*No changelog for this release.*

---

## v1.1.25 (26/07/2019)

#### 🐛 Bug Fixes

-  fix: use cmd param in the gitCmd step [#157](https://github.com/elastic/apm-pipeline-library/pull/157)

---

## v1.1.24 (26/07/2019)
*No changelog for this release.*

---

## v1.1.23 (26/07/2019)
*No changelog for this release.*

---

## v1.1.22 (26/07/2019)

#### 🚀 Enhancements

-  feat: rename GitHub check as discussed [#155](https://github.com/elastic/apm-pipeline-library/pull/155)

#### 🐛 Bug Fixes

-  fix null pointer exceptions when calling the method from another step [#156](https://github.com/elastic/apm-pipeline-library/pull/156)

---

## v1.1.21 (26/07/2019)

#### 🚀 Enhancements

-  enable junit reporting for the pre-commit stage [#154](https://github.com/elastic/apm-pipeline-library/pull/154)

---

## v1.1.20 (26/07/2019)

#### 🚀 Enhancements

-  feat: preCommitToJunit step [#153](https://github.com/elastic/apm-pipeline-library/pull/153)

#### 🐛 Bug Fixes

-  fix: use alias to avoid hit erased paths [#141](https://github.com/elastic/apm-pipeline-library/pull/141)
-  fix: execute .ci/scripts/push-integration-test-images.sh in the correct context [#142](https://github.com/elastic/apm-pipeline-library/pull/142)
-  fix: we have to use the raw output of jq [#144](https://github.com/elastic/apm-pipeline-library/pull/144)
-  fix: make gren docker image works [#140](https://github.com/elastic/apm-pipeline-library/pull/140)
-  fix: grab the real version name from artifactory before push [#143](https://github.com/elastic/apm-pipeline-library/pull/143)

#### ⚙️ CI

-  feat: add docker images to the packer cache [#152](https://github.com/elastic/apm-pipeline-library/pull/152)
-  fix: used trustworthy Docker images [#151](https://github.com/elastic/apm-pipeline-library/pull/151)
-  fix: post step is in the wrong place [#147](https://github.com/elastic/apm-pipeline-library/pull/147)
-  fix: make the checkout of the repo [#146](https://github.com/elastic/apm-pipeline-library/pull/146)
-  fix: remove documentation stage and related steps [#145](https://github.com/elastic/apm-pipeline-library/pull/145)

---

## v1.1.19 (19/07/2019)
*No changelog for this release.*

---

## v1.1.18 (19/07/2019)

#### 🚀 Enhancements

-  GitHub comment trigger parser step [#139](https://github.com/elastic/apm-pipeline-library/pull/139)
-  feat: backport config [#137](https://github.com/elastic/apm-pipeline-library/pull/137)
-  enable issueCommentTrigger and expose GITHUB_COMMENT environment [#136](https://github.com/elastic/apm-pipeline-library/pull/136)
-  enable cobertura and gsc tabs with the traditional view [#135](https://github.com/elastic/apm-pipeline-library/pull/135)
-  ci: release process [#123](https://github.com/elastic/apm-pipeline-library/pull/123)

#### 🐛 Bug Fixes

-  fix net.sf.json.JSONNull.trim() [#138](https://github.com/elastic/apm-pipeline-library/pull/138)
-  fix: log rotate settings [#131](https://github.com/elastic/apm-pipeline-library/pull/131)

#### 🙈 No user affected

-  disable opbeans-dotnet build images generation [#134](https://github.com/elastic/apm-pipeline-library/pull/134)

---

## v1.1.17 (17/07/2019)

#### 🚀 Enhancements

-  feat: git commands steps [#132](https://github.com/elastic/apm-pipeline-library/pull/132)
-  exclude target folder from the pre-commit analysis [#133](https://github.com/elastic/apm-pipeline-library/pull/133)
-  feat: build, test, and push integration testing Docker images [#129](https://github.com/elastic/apm-pipeline-library/pull/129)
-  feat: add description to the notification email [#130](https://github.com/elastic/apm-pipeline-library/pull/130)

#### 🐛 Bug Fixes

-  fix: shellcheck and yamllint installations for the pre-commit stage [#128](https://github.com/elastic/apm-pipeline-library/pull/128)
-  fix: avoid show 'null' on the email [#126](https://github.com/elastic/apm-pipeline-library/pull/126)
-  fix: run tests with JDK 11 [#125](https://github.com/elastic/apm-pipeline-library/pull/125)
-  fix the deploy as it was done previously [#120](https://github.com/elastic/apm-pipeline-library/pull/120)

#### 📚 Documentation

-  Update readme with cli tool to run Jenkinsfiles locally [#122](https://github.com/elastic/apm-pipeline-library/pull/122)

#### 🙈 No user affected

-  disable opbeans-flask as stated in  [#127](https://github.com/elastic/apm-pipeline-library/pull/127)
-  chore: temporary pipeline for testing purposes [#121](https://github.com/elastic/apm-pipeline-library/pull/121)

---

## v1.1.16 (10/07/2019)

#### 🚀 Enhancements

-  feat: add more precommit hooks [#119](https://github.com/elastic/apm-pipeline-library/pull/119)
-  pre-commit: lint pipelines which are stored in the .ci folder and add pre-commit stage in the CI [#118](https://github.com/elastic/apm-pipeline-library/pull/118)
-  feat: download 7.3.0 Elastic Stack Docker images [#116](https://github.com/elastic/apm-pipeline-library/pull/116)
-  Simplify POM [#90](https://github.com/elastic/apm-pipeline-library/pull/90)

#### 🐛 Bug Fixes

-  fix: notify gh check when it's triggered as a cron type [#117](https://github.com/elastic/apm-pipeline-library/pull/117)

#### 🙈 No user affected

-  feat: enforce the user and update CHANGELOG.md with latest release [#114](https://github.com/elastic/apm-pipeline-library/pull/114)

---

## v1.1.15 (05/07/2019)

#### 🚀 Enhancements

-  feat: setup local development [#97](https://github.com/elastic/apm-pipeline-library/pull/97)
-  feat: generate release notes and changelog from PRs [#103](https://github.com/elastic/apm-pipeline-library/pull/103)
-  feat: retry Docker login on failure 3 times [#111](https://github.com/elastic/apm-pipeline-library/pull/111)

#### 🐛 Bug Fixes

-  fix: throw an error when GitHub API call fails [#110](https://github.com/elastic/apm-pipeline-library/pull/110)
-  fix: change .jenkins_python.yml file path [#106](https://github.com/elastic/apm-pipeline-library/pull/106)
-  fix: change cluster job names on the weekly job [#102](https://github.com/elastic/apm-pipeline-library/pull/102)

#### ⚙️ CI

-  ci: ignore stage failures and continue [#108](https://github.com/elastic/apm-pipeline-library/pull/108)
-  feat: retry in case vault is not reachable [#107](https://github.com/elastic/apm-pipeline-library/pull/107)
-  ci: remove quiet period on cluster updates [#104](https://github.com/elastic/apm-pipeline-library/pull/104)
-  ci: disable SCM trigger for Observability test environments jobs [#105](https://github.com/elastic/apm-pipeline-library/pull/105)
-  ci: Cache Oracle Instant Client Docker Image [#100](https://github.com/elastic/apm-pipeline-library/pull/100)

---

## v1.1.14 (28/06/2019)

#### 🐛 Bug Fixes

-  fix: githubNotify when success should not happen when notify is disabled [#101](https://github.com/elastic/apm-pipeline-library/pull/101)

---

## v1.1.13 (27/06/2019)

#### 🚀 Enhancements

-  feat: notify first time contributor github check [#98](https://github.com/elastic/apm-pipeline-library/pull/98)

#### ⚙️ CI

-  ci: refactor ITs maps to be centralised and being reused if required [#95](https://github.com/elastic/apm-pipeline-library/pull/95)

---

## v1.1.12 (27/06/2019)

#### 🐛 Bug Fixes

-  fix: master worker bear hug [#99](https://github.com/elastic/apm-pipeline-library/pull/99)

---

## v1.1.11 (26/06/2019)

#### 🚀 Enhancements

-  feat: update k8s clusters from CI on Mondays [#93](https://github.com/elastic/apm-pipeline-library/pull/93)
-  [APM-CI] Provide JUnit report for checkLicenses [#77](https://github.com/elastic/apm-pipeline-library/pull/77)

#### 🐛 Bug Fixes

-  fix: add pipeline to the job link, remove double slash from URLs [#96](https://github.com/elastic/apm-pipeline-library/pull/96)
-  fix: notify on cleanup stage [#94](https://github.com/elastic/apm-pipeline-library/pull/94)

---

## v1.1.10 (24/06/2019)

#### 🐛 Bug Fixes

-  fix: adds protocol twice [#92](https://github.com/elastic/apm-pipeline-library/pull/92)

---

## v1.1.9 (21/06/2019)

#### 🐛 Bug Fixes

-  fix: execute getVaultSecret inside a node [#91](https://github.com/elastic/apm-pipeline-library/pull/91)

---

## v1.1.8 (21/06/2019)

#### 🐛 Bug Fixes

-  fix: add parameter to catchError to avoid break the build on a notification error [#89](https://github.com/elastic/apm-pipeline-library/pull/89)

#### 📚 Documentation

-  chore: update README about make a library release [#87](https://github.com/elastic/apm-pipeline-library/pull/87)

---

## v1.1.7 (19/06/2019)
*No changelog for this release.*

---

## v1.1.6 (19/06/2019)

#### 🚀 Enhancements

-  feat: pipeline to update test environments [#86](https://github.com/elastic/apm-pipeline-library/pull/86)

#### 🐛 Bug Fixes

-  fix: change deprecated call to getVaultSecrets to the current [#85](https://github.com/elastic/apm-pipeline-library/pull/85)

#### 📚 Documentation

-  (#82) Update docs about releasing a new version [#83](https://github.com/elastic/apm-pipeline-library/pull/83)

#### 🙈 No user affected

-  refactor: change credentials to access to EC [#84](https://github.com/elastic/apm-pipeline-library/pull/84)

---

## v1.1.5 (14/06/2019)

#### 🚀 Enhancements

-  [APM-CI] checkLicenses step [#76](https://github.com/elastic/apm-pipeline-library/pull/76)

#### 🐛 Bug Fixes

-  (#80) Fix gitCheckout when the caller passed mergeTarget [#81](https://github.com/elastic/apm-pipeline-library/pull/81)
-  [APM-CI] Powershell in W2016 returns a different output [#79](https://github.com/elastic/apm-pipeline-library/pull/79)

---

## v1.1.4 (12/06/2019)

#### 🐛 Bug Fixes

-  [APM-CI] Cosmetic changes: README and Pipeline [#75](https://github.com/elastic/apm-pipeline-library/pull/75)
-  fix: login on the Docker Elastic registry before to push [#74](https://github.com/elastic/apm-pipeline-library/pull/74)

---

## v1.1.3 (10/06/2019)

#### 🐛 Bug Fixes

-  fix: avoid showing 'null' on the email subject when BRANCH_NAME is not defined [#73](https://github.com/elastic/apm-pipeline-library/pull/73)
-  fix: check the return value of curl instead of the file is created [#72](https://github.com/elastic/apm-pipeline-library/pull/72)

---

## v1.1.2 (10/06/2019)

#### 🚀 Enhancements

-  feat: add different GitHub context for each stage to test how it behaves [#71](https://github.com/elastic/apm-pipeline-library/pull/71)

#### 🐛 Bug Fixes

-  Fix notifications on weird inputs [#70](https://github.com/elastic/apm-pipeline-library/pull/70)

---

## v1.1.1 (07/06/2019)

#### 🐛 Bug Fixes

-  fix: protect against some posibles NPE or undefined methods errors [#69](https://github.com/elastic/apm-pipeline-library/pull/69)

---

## v1.1.0 (07/06/2019)

#### 🚀 Enhancements

-   feat: scheduled tasks [#68](https://github.com/elastic/apm-pipeline-library/pull/68)
-  feat: add build info to the test results object [#65](https://github.com/elastic/apm-pipeline-library/pull/65)
-  feat: new job for Integrations registry repo [#64](https://github.com/elastic/apm-pipeline-library/pull/64)

#### 🐛 Bug Fixes

-  fix: protect against null values on changes fields [#67](https://github.com/elastic/apm-pipeline-library/pull/67)
-  fix: remove job that it is in the beats-ci too [#66](https://github.com/elastic/apm-pipeline-library/pull/66)
-  fix: fix scm checkout on dockerImagesESLatest pipeline [#63](https://github.com/elastic/apm-pipeline-library/pull/63)

---

## v1.0.22 (03/06/2019)

#### ⚙️ CI

-  ci(jenkins): add a condition to send or not the emails [#62](https://github.com/elastic/apm-pipeline-library/pull/62)

---

## v1.0.21 (03/06/2019)

#### 🚀 Enhancements

-  ci(jenkins): new Notifications and report to Elasticsearch [#61](https://github.com/elastic/apm-pipeline-library/pull/61)

---

## v1.0.20 (03/06/2019)

#### 🚀 Enhancements

-  [APM-CI] WithGithubNotify wrapper step [#60](https://github.com/elastic/apm-pipeline-library/pull/60)

---

## v1.0.19 (31/05/2019)

#### 🚀 Enhancements

-  [APM-CI][.NET] Support .NET images [#56](https://github.com/elastic/apm-pipeline-library/pull/56)
-  [APM-CI][All] Make the trigger by comment case-insensitive [#50](https://github.com/elastic/apm-pipeline-library/pull/50)
-  ci(jenkins): add licenses to files, fix JJBB jobs [#59](https://github.com/elastic/apm-pipeline-library/pull/59)
-  (#53) Include Maven wrapper into the scm [#54](https://github.com/elastic/apm-pipeline-library/pull/54)
-  [ci] add files for JJBB jobs [#49](https://github.com/elastic/apm-pipeline-library/pull/49)
-  [APM-CI][Ruby] Build Docker images for JRuby [#48](https://github.com/elastic/apm-pipeline-library/pull/48)
-  [APM-CI] Implement a withSecretVault step [#51](https://github.com/elastic/apm-pipeline-library/pull/51)

#### 📚 Documentation

-  doc: update README and steps README [#55](https://github.com/elastic/apm-pipeline-library/pull/55)

#### ⚙️ CI

-  ci(jenkins): Update pipelines [#58](https://github.com/elastic/apm-pipeline-library/pull/58)
-  ci(jenkins): pipelines to build/update Docker images [#57](https://github.com/elastic/apm-pipeline-library/pull/57)

---

## v1.0.18 (20/05/2019)

#### 🐛 Bug Fixes

-  Develop updates [#47](https://github.com/elastic/apm-pipeline-library/pull/47)

---

## v1.0.17 (22/04/2019)
*No changelog for this release.*

---

## v1.0.16 (11/04/2019)
*No changelog for this release.*

---

## v1.0.15 (10/04/2019)

#### 🚀 Enhancements

-  [APM-CI] use environment variables for user and password [#46](https://github.com/elastic/apm-pipeline-library/pull/46)

#### 🐛 Bug Fixes

-  [APM-CI] hide dockerLogin output [#45](https://github.com/elastic/apm-pipeline-library/pull/45)

---

## v1.0.14 (08/04/2019)

#### 🐛 Bug Fixes

-  Develop [#44](https://github.com/elastic/apm-pipeline-library/pull/44)

---

## v1.0.13 (04/04/2019)
*No changelog for this release.*

---

## v1.0.12 (01/04/2019)

#### 🚀 Enhancements

-  [APM-CI] add cache to GitHub API REST calls [#42](https://github.com/elastic/apm-pipeline-library/pull/42)
-  [APM-CI] add token cache to the codecov step [#41](https://github.com/elastic/apm-pipeline-library/pull/41)

---

## v1.0.11 (22/03/2019)

#### 🚀 Enhancements

-  Develop [#40](https://github.com/elastic/apm-pipeline-library/pull/40)

---

## v1.0.10 (19/03/2019)

#### 🐛 Bug Fixes

-  [APM-CI] get commit sha before merge on PRs [#39](https://github.com/elastic/apm-pipeline-library/pull/39)

---

## v1.0.9 (01/03/2019)
*No changelog for this release.*

---

## v1.0.8 (27/02/2019)

#### ⚙️ CI

-  [APM-CI] test Jenkins agents capabilities [#37](https://github.com/elastic/apm-pipeline-library/pull/37)
-  [APM-CI] Add test pipeline [#36](https://github.com/elastic/apm-pipeline-library/pull/36)

---

## v1.0.7 (26/02/2019)

#### 🚀 Enhancements

-  [APM-CI] Support bot PRs [#34](https://github.com/elastic/apm-pipeline-library/pull/34)

#### 🙈 No user affected

-  [APM-CI] Refactor [#32](https://github.com/elastic/apm-pipeline-library/pull/32)

---

## v1.0.6 (30/01/2019)
*No changelog for this release.*

---

## v1.0.5 (23/01/2019)

#### 🐛 Bug Fixes

-  [APM-CI] if there are rejected reviews it fails [#29](https://github.com/elastic/apm-pipeline-library/pull/29)

---

## v1.0.4 (15/01/2019)

#### 🚀 Enhancements

-   [APM-CI] add support to reference repository to gitCheckout [#27](https://github.com/elastic/apm-pipeline-library/pull/27)
-  Add "flags" param to codecov [#26](https://github.com/elastic/apm-pipeline-library/pull/26)

#### 📚 Documentation

-  [APM-CI] Reference repo kibana/Elasticsearch [#28](https://github.com/elastic/apm-pipeline-library/pull/28)

---

## v1.0.3 (09/01/2019)

#### 🚀 Enhancements

-  [APM-CI] New steps httpRequest and toJSON [#23](https://github.com/elastic/apm-pipeline-library/pull/23)
-   allow manual build triggered always [#21](https://github.com/elastic/apm-pipeline-library/pull/21)

#### 🐛 Bug Fixes

-  [APM-CI] Fix Github API calls [#25](https://github.com/elastic/apm-pipeline-library/pull/25)

#### 📚 Documentation

-  [APM-CI] document the checkout process [#22](https://github.com/elastic/apm-pipeline-library/pull/22)

---

## v1.0.2 (21/12/2018)

#### 🚀 Enhancements

-  Check approved [#16](https://github.com/elastic/apm-pipeline-library/pull/16)
-  step to run from inline pipelines to allow run pipelines from the library [#2](https://github.com/elastic/apm-pipeline-library/pull/2)
-  Checkout elastic docs tools tests [#3](https://github.com/elastic/apm-pipeline-library/pull/3)
-  Make fetch [#5](https://github.com/elastic/apm-pipeline-library/pull/5)
-  GitHub env step [#13](https://github.com/elastic/apm-pipeline-library/pull/13)
-  adapt Jenkinsfile to use gitCheckout step [#8](https://github.com/elastic/apm-pipeline-library/pull/8)

#### 🐛 Bug Fixes

-  wrong user reference [#20](https://github.com/elastic/apm-pipeline-library/pull/20)
-  Github REST API call error management [#19](https://github.com/elastic/apm-pipeline-library/pull/19)
-  Trim strings [#10](https://github.com/elastic/apm-pipeline-library/pull/10)

#### 📚 Documentation

-  link on main page to the steps documentation [#18](https://github.com/elastic/apm-pipeline-library/pull/18)
-  Update template [#11](https://github.com/elastic/apm-pipeline-library/pull/11)
-  Jenkinsfile template [#9](https://github.com/elastic/apm-pipeline-library/pull/9)

#### 🙈 No user affected

-  Delete no used steps [#12](https://github.com/elastic/apm-pipeline-library/pull/12)

#### ⚙️ CI

-  [APM-CI] APM UI pipeline version 0.2 [#15](https://github.com/elastic/apm-pipeline-library/pull/15)
-  [APM-CI] APM UI pipeline version 0.1 [#14](https://github.com/elastic/apm-pipeline-library/pull/14)
-  Fix test and add new ones [#17](https://github.com/elastic/apm-pipeline-library/pull/17)
-  [APM-CI] APM UI pipeline version 0 [#4](https://github.com/elastic/apm-pipeline-library/pull/4)
-  Pr no build test [#7](https://github.com/elastic/apm-pipeline-library/pull/7)
-  Jenkinsfile [#1](https://github.com/elastic/apm-pipeline-library/pull/1)
-  Pr no build test [#6](https://github.com/elastic/apm-pipeline-library/pull/6)
