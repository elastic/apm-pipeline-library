# Changelog

## v1.1.180 (26/01/2021)

#### 🚀 Enhancements

-  [beats][go] support for ARM arch64 [#924](https://github.com/elastic/apm-pipeline-library/pull/924)
-  Create GitHub Check with the digested Build data [#921](https://github.com/elastic/apm-pipeline-library/pull/921)
-  GitHub check step [#881](https://github.com/elastic/apm-pipeline-library/pull/881)

#### 🐛 Bug Fixes

-  Delegate GitHub PR comments condition to the githubPrComment step [#925](https://github.com/elastic/apm-pipeline-library/pull/925)
-  fix: retry wait for filebeat [#922](https://github.com/elastic/apm-pipeline-library/pull/922)
-  Mask the GitHub app generated token [#920](https://github.com/elastic/apm-pipeline-library/pull/920)

## v1.1.179 (25/01/2021)

#### 🚀 Enhancements

-  feat: example job to manane webhooks [#912](https://github.com/elastic/apm-pipeline-library/pull/912)

#### 🐛 Bug Fixes

-  Skip GitHub comment for old build when the next adjacent build has already finished [#917](https://github.com/elastic/apm-pipeline-library/pull/917)

#### 🙈 No user affected

-  [refactor] notifyBuildResult [#916](https://github.com/elastic/apm-pipeline-library/pull/916)

## v1.1.178 (20/01/2021)

#### 🚀 Enhancements

-  [pre-commit] exclude gif/png files [#909](https://github.com/elastic/apm-pipeline-library/pull/909)

#### 🐛 Bug Fixes

-  Add resilience when GitHub got some glitches [#911](https://github.com/elastic/apm-pipeline-library/pull/911)

#### ⚙️ CI

-  feat: add a pre-commit hook detecting broken markdown links [#914](https://github.com/elastic/apm-pipeline-library/pull/914)

## v1.1.177 (15/01/2021)

#### 🚀 Enhancements

-  feat: use goDefaultVersion [#903](https://github.com/elastic/apm-pipeline-library/pull/903)
-  feat: add stackVersions step [#902](https://github.com/elastic/apm-pipeline-library/pull/902)
-  feat: goDefaultVersion step [#901](https://github.com/elastic/apm-pipeline-library/pull/901)

## v1.1.176 (13/01/2021)

#### 🐛 Bug Fixes

-  fix: use current folder instead of the workspace [#900](https://github.com/elastic/apm-pipeline-library/pull/900)
-  Fix issue when there is a build corruption with the GH PR comment [#899](https://github.com/elastic/apm-pipeline-library/pull/899)

## v1.1.175 (13/01/2021)

#### 🐛 Bug Fixes

-  fix: use WS always [#898](https://github.com/elastic/apm-pipeline-library/pull/898)

## v1.1.174 (12/01/2021)
*No changelog for this release.*

## v1.1.173 (12/01/2021)

#### 🐛 Bug Fixes

-  fix: use docker stop not kill [#895](https://github.com/elastic/apm-pipeline-library/pull/895)

## v1.1.172 (11/01/2021)
*No changelog for this release.*

## v1.1.171 (11/01/2021)

#### 🚀 Enhancements

-  Support not_changeset_full_match for beats [#890](https://github.com/elastic/apm-pipeline-library/pull/890)

#### 🐛 Bug Fixes

-  fix: wait for docker exec to finish [#893](https://github.com/elastic/apm-pipeline-library/pull/893)

#### 🙈 No user affected

-  [CI][mvn4] Use lastSuccessfulBuild [#894](https://github.com/elastic/apm-pipeline-library/pull/894)

## v1.1.170 (07/01/2021)

#### 🚀 Enhancements

-  [beats] Support changeset exclusion [#889](https://github.com/elastic/apm-pipeline-library/pull/889)
-  feat: filebeat pipeline step [#887](https://github.com/elastic/apm-pipeline-library/pull/887)
-  feat: new goTestJunit step [#884](https://github.com/elastic/apm-pipeline-library/pull/884)

#### 🙈 No user affected

-  chore: rename argument [#885](https://github.com/elastic/apm-pipeline-library/pull/885)
-  Use maven 4 alpha [#879](https://github.com/elastic/apm-pipeline-library/pull/879)

#### ⚙️ CI

-  [opbeans-php] Add docker build and cache [#888](https://github.com/elastic/apm-pipeline-library/pull/888)

## v1.1.169 (16/12/2020)

#### 🚀 Enhancements

-  Support Windows for withGoEnv [#878](https://github.com/elastic/apm-pipeline-library/pull/878)

## v1.1.168 (16/12/2020)

#### 🚀 Enhancements

-  Support withHubCredentials step [#871](https://github.com/elastic/apm-pipeline-library/pull/871)

#### 🙈 No user affected

-  [beats] flaky weekly email per branch [#876](https://github.com/elastic/apm-pipeline-library/pull/876)

## v1.1.167 (08/12/2020)

#### 🚀 Enhancements

-  [build][data] Enable data related to PRs branches/targets [#875](https://github.com/elastic/apm-pipeline-library/pull/875)

## v1.1.166 (04/12/2020)

#### 🙈 No user affected

-  [report] report one error signal if multiple [#870](https://github.com/elastic/apm-pipeline-library/pull/870)
-  [daily] build docker images [#868](https://github.com/elastic/apm-pipeline-library/pull/868)

#### ⚙️ CI

-  [CI] runbld is now deprecated [#869](https://github.com/elastic/apm-pipeline-library/pull/869)

## v1.1.165 (30/11/2020)

#### 🐛 Bug Fixes

-  Fix isBranch type [#865](https://github.com/elastic/apm-pipeline-library/pull/865)

## v1.1.164 (25/11/2020)

#### 🙈 No user affected

-  [index] populate only test failures [#864](https://github.com/elastic/apm-pipeline-library/pull/864)
-  fix archive files inside .git folder [#863](https://github.com/elastic/apm-pipeline-library/pull/863)
-  [ignore] gitcmd files with the .git folder [#862](https://github.com/elastic/apm-pipeline-library/pull/862)

## v1.1.163 (25/11/2020)

#### 🚀 Enhancements

-  Add certain latency to access to the google storage [#855](https://github.com/elastic/apm-pipeline-library/pull/855)

#### 🐛 Bug Fixes

-  Flaky causes NPE [#849](https://github.com/elastic/apm-pipeline-library/pull/849)

#### 🙈 No user affected

-  [index] bulk format and remove certain unused fields [#860](https://github.com/elastic/apm-pipeline-library/pull/860)
-  If no GH Issues then use flaky test details [#858](https://github.com/elastic/apm-pipeline-library/pull/858)
-  Bulk update with a flatten tests [#856](https://github.com/elastic/apm-pipeline-library/pull/856)

#### ⚙️ CI

-  chore: simplify RUM JS image [#859](https://github.com/elastic/apm-pipeline-library/pull/859)
-  chore: add RUM images to the nightly build [#851](https://github.com/elastic/apm-pipeline-library/pull/851)

## v1.1.162 (17/11/2020)

#### 🐛 Bug Fixes

-  Skip notifications if no notifications are in the list to send [#847](https://github.com/elastic/apm-pipeline-library/pull/847)

## v1.1.161 (16/11/2020)

#### 🚀 Enhancements

-  Aggregate GH Comments [#841](https://github.com/elastic/apm-pipeline-library/pull/841)

#### 🙈 No user affected

-  Revert "New ES index" [#842](https://github.com/elastic/apm-pipeline-library/pull/842)
-  [notification] skip steps comment in slack if none [#845](https://github.com/elastic/apm-pipeline-library/pull/845)
-  Remove duplicated metadata [#840](https://github.com/elastic/apm-pipeline-library/pull/840)
-  [local] docker instance with fixed version [#838](https://github.com/elastic/apm-pipeline-library/pull/838)

## v1.1.160 (11/11/2020)

#### 🙈 No user affected

-  Add env variable to easily search in the future [#836](https://github.com/elastic/apm-pipeline-library/pull/836)
-  Exclude _class,_links,latestRun,permissions and parameters to be populated [#831](https://github.com/elastic/apm-pipeline-library/pull/831)
-  [cosmetic] simplify test errors report [#830](https://github.com/elastic/apm-pipeline-library/pull/830)
-  [dependabot] disable docker images [#827](https://github.com/elastic/apm-pipeline-library/pull/827)
-  [dependabot] Enable for maven and docker images [#809](https://github.com/elastic/apm-pipeline-library/pull/809)

#### ⚙️ CI

-  Support runWatcher and run on a weekly basis [#833](https://github.com/elastic/apm-pipeline-library/pull/833)
-  Add docker build for bandstand orch [#821](https://github.com/elastic/apm-pipeline-library/pull/821)

## v1.1.159 (03/11/2020)

#### 🐛 Bug Fixes

-  Fix git diff for first builds in branches [#808](https://github.com/elastic/apm-pipeline-library/pull/808)
-  GH comment with links to the the entire logs and cosmetic changes [#801](https://github.com/elastic/apm-pipeline-library/pull/801)
-  cosmetic change to highlight when disabled [#800](https://github.com/elastic/apm-pipeline-library/pull/800)

#### 🙈 No user affected

-  cosmetic: code snippet and conditions [#807](https://github.com/elastic/apm-pipeline-library/pull/807)
-  [test-infra] validate yq installation [#804](https://github.com/elastic/apm-pipeline-library/pull/804)
-  [test-infra]  windows version [#802](https://github.com/elastic/apm-pipeline-library/pull/802)

#### ⚙️ CI

-  ci: cover Kibana PRs from 7*10^4 up to 10^7-1 [#799](https://github.com/elastic/apm-pipeline-library/pull/799)

## v1.1.158 (29/10/2020)

#### 🐛 Bug Fixes

-  Fix cache for GH comments [#798](https://github.com/elastic/apm-pipeline-library/pull/798)

## v1.1.157 (27/10/2020)

#### 🚀 Enhancements

-  [flaky] report Flaky Tests in GitHub automatically [#791](https://github.com/elastic/apm-pipeline-library/pull/791)
-  [flaky] report genuine test failures [#792](https://github.com/elastic/apm-pipeline-library/pull/792)

#### 🙈 No user affected

-  [flaky] fix markdown formatting [#795](https://github.com/elastic/apm-pipeline-library/pull/795)
-  [flaky] enable for the apm-pipeline-library [#781](https://github.com/elastic/apm-pipeline-library/pull/781)
-  [test-infra] gh not validated on Windows [#788](https://github.com/elastic/apm-pipeline-library/pull/788)
-  [CI] windows-2019 got python2 newer [#786](https://github.com/elastic/apm-pipeline-library/pull/786)

## v1.1.156 (22/10/2020)

#### 🐛 Bug Fixes

-  fix: make Docker login on the nodes [#787](https://github.com/elastic/apm-pipeline-library/pull/787)
-  fix: reference expression [#785](https://github.com/elastic/apm-pipeline-library/pull/785)

#### 🙈 No user affected

-  [test-infra] gh validation [#777](https://github.com/elastic/apm-pipeline-library/pull/777)

## v1.1.155 (22/10/2020)

#### 🚀 Enhancements

-  Install gh if not available [#782](https://github.com/elastic/apm-pipeline-library/pull/782)

## v1.1.154 (21/10/2020)

#### 🚀 Enhancements

-  [flaky] categorise comments in failed or not failed tests [#779](https://github.com/elastic/apm-pipeline-library/pull/779)

#### ⚙️ CI

-  fix: clean Docker images after push [#780](https://github.com/elastic/apm-pipeline-library/pull/780)

## v1.1.153 (19/10/2020)

#### 🚀 Enhancements

-  Support PHP ITs [#778](https://github.com/elastic/apm-pipeline-library/pull/778)

## v1.1.152 (19/10/2020)

#### 🚀 Enhancements

-  Support API calls without caching [#776](https://github.com/elastic/apm-pipeline-library/pull/776)

#### 🙈 No user affected

-  Avoid warning in JUnit when using args that are not defined [#775](https://github.com/elastic/apm-pipeline-library/pull/775)

## v1.1.151 (16/10/2020)

#### 🚀 Enhancements

-  [flaky] enable if no aborted [#773](https://github.com/elastic/apm-pipeline-library/pull/773)

#### 🐛 Bug Fixes

-  Avoid using windows immutable workers [#774](https://github.com/elastic/apm-pipeline-library/pull/774)

## v1.1.150 (15/10/2020)

#### 🚀 Enhancements

-  [flaky] Control number of issues to be created [#771](https://github.com/elastic/apm-pipeline-library/pull/771)
-  [notification] show only up to 10 tests/steps failures [#772](https://github.com/elastic/apm-pipeline-library/pull/772)
-  Enable dependabot[bot] [#770](https://github.com/elastic/apm-pipeline-library/pull/770)
-  Flaky test comments [#754](https://github.com/elastic/apm-pipeline-library/pull/754)
-  Grant running access for the dependabot user [#769](https://github.com/elastic/apm-pipeline-library/pull/769)
-  cosmetic change slack comment [#768](https://github.com/elastic/apm-pipeline-library/pull/768)

#### 🙈 No user affected

-  [test-infra] add new darwin workers and slack notifications [#767](https://github.com/elastic/apm-pipeline-library/pull/767)

## v1.1.149 (13/10/2020)

#### 🚀 Enhancements

-  Return PR URL with githubCreatePullRequest step [#765](https://github.com/elastic/apm-pipeline-library/pull/765)

## v1.1.148 (12/10/2020)

#### 🚀 Enhancements

-  slack message for no push events [#762](https://github.com/elastic/apm-pipeline-library/pull/762)

#### 🙈 No user affected

-  [DOCS] fix typo [#758](https://github.com/elastic/apm-pipeline-library/pull/758)
-  [DOCS] Add badge details [#757](https://github.com/elastic/apm-pipeline-library/pull/757)

#### ⚙️ CI

-  ci: add workers to the infra test [#759](https://github.com/elastic/apm-pipeline-library/pull/759)

## v1.1.147 (06/10/2020)

#### 🚀 Enhancements

-  highlight step and test failures in GH comments [#752](https://github.com/elastic/apm-pipeline-library/pull/752)

## v1.1.146 (05/10/2020)

#### 🚀 Enhancements

-  Resilience when test-summary is not accessible. [#751](https://github.com/elastic/apm-pipeline-library/pull/751)

## v1.1.145 (05/10/2020)

#### 🚀 Enhancements

-  isBranch and isTag helpers [#749](https://github.com/elastic/apm-pipeline-library/pull/749)

#### 🙈 No user affected

-  [cosmetic] less verbose test and step failures in the slack message [#750](https://github.com/elastic/apm-pipeline-library/pull/750)
-  [local] remove resources limitations in docker [#748](https://github.com/elastic/apm-pipeline-library/pull/748)

## v1.1.144 (02/10/2020)

#### 🚀 Enhancements

-  Exclude GH Notifies and JUnit from reported failed steps [#747](https://github.com/elastic/apm-pipeline-library/pull/747)
-  Support slack comments for builds [#745](https://github.com/elastic/apm-pipeline-library/pull/745)

#### 🐛 Bug Fixes

-  Fix 500 http errors in BO [#746](https://github.com/elastic/apm-pipeline-library/pull/746)

## v1.1.143 (30/09/2020)

#### 🚀 Enhancements

-  feat: package and release for Python modules [#730](https://github.com/elastic/apm-pipeline-library/pull/730)

#### 🐛 Bug Fixes

-  fix branch when on a PR basis [#737](https://github.com/elastic/apm-pipeline-library/pull/737)

## v1.1.142 (29/09/2020)

#### 🚀 Enhancements

-  Support skip-ci-build label for beatsWhen [#733](https://github.com/elastic/apm-pipeline-library/pull/733)

#### 🐛 Bug Fixes

-  Fix docker-compose and JCasC [#735](https://github.com/elastic/apm-pipeline-library/pull/735)

#### 🙈 No user affected

-  reduce build reasons i/o accesses with global variables [#734](https://github.com/elastic/apm-pipeline-library/pull/734)

## v1.1.141 (24/09/2020)

#### 🚀 Enhancements

-  Support ARM arch [#731](https://github.com/elastic/apm-pipeline-library/pull/731)

## v1.1.140 (24/09/2020)

#### 🚀 Enhancements

-  runbld wrappers [#728](https://github.com/elastic/apm-pipeline-library/pull/728)
-  test: POC of APM CLI [#715](https://github.com/elastic/apm-pipeline-library/pull/715)

## v1.1.139 (22/09/2020)

#### 🚀 Enhancements

-  isInternalCI step [#727](https://github.com/elastic/apm-pipeline-library/pull/727)

#### 🐛 Bug Fixes

-  Remove folder jobdsl reference [#725](https://github.com/elastic/apm-pipeline-library/pull/725)

## v1.1.138 (15/09/2020)

#### 🐛 Bug Fixes

-  [test-infra] fix pytests with multimodule [#722](https://github.com/elastic/apm-pipeline-library/pull/722)

#### ⚙️ CI

- [**on-hold**] refactor: use env variables from the plugin [#426](https://github.com/elastic/apm-pipeline-library/pull/426)

## v1.1.137 (14/09/2020)

#### 🚀 Enhancements

-  Support multiple teams validation [#721](https://github.com/elastic/apm-pipeline-library/pull/721)

#### 🐛 Bug Fixes

-  Fix isMemberOf data structure [#720](https://github.com/elastic/apm-pipeline-library/pull/720)

## v1.1.136 (14/09/2020)

#### 🚀 Enhancements

-  2.0 pipeline steps for beats [#689](https://github.com/elastic/apm-pipeline-library/pull/689)
-  Remove timeout analyser and rebuild [#708](https://github.com/elastic/apm-pipeline-library/pull/708)

#### ⚙️ CI

-  Move to fe/be config for apm-proxy [#718](https://github.com/elastic/apm-pipeline-library/pull/718)
-  Wrap checkout of spoa in dir [#717](https://github.com/elastic/apm-pipeline-library/pull/717)
-  chore: automate pickes [#714](https://github.com/elastic/apm-pipeline-library/pull/714)
-  New stage for apm-proxy [#712](https://github.com/elastic/apm-pipeline-library/pull/712)

## v1.1.135 (04/09/2020)

#### 🚀 Enhancements

-  isMemberOf step to query GitHub user/teams [#711](https://github.com/elastic/apm-pipeline-library/pull/711)

#### 🙈 No user affected

-  [test-infra] run docker if docker-machine installed [#710](https://github.com/elastic/apm-pipeline-library/pull/710)

## v1.1.134 (18/08/2020)

#### ⚙️ CI

-  [CI] git commit with correct user.email [#700](https://github.com/elastic/apm-pipeline-library/pull/700)

## v1.1.133 (18/08/2020)

#### 🐛 Bug Fixes

-  fix read only env variable [#698](https://github.com/elastic/apm-pipeline-library/pull/698)

## v1.1.132 (17/08/2020)

#### 🐛 Bug Fixes

-  Fix opbeans-frontend release process [#697](https://github.com/elastic/apm-pipeline-library/pull/697)
-  Use real email account [#695](https://github.com/elastic/apm-pipeline-library/pull/695)

## v1.1.131 (12/08/2020)

#### 🐛 Bug Fixes

-  Control when to send email notifications [#692](https://github.com/elastic/apm-pipeline-library/pull/692)

#### ⚙️ CI

-  feat: support passing custom build commands from the target project [#683](https://github.com/elastic/apm-pipeline-library/pull/683)
-  fix: typo caused by keyboard strokes [#688](https://github.com/elastic/apm-pipeline-library/pull/688)
-  [jjbb] beats mbp is not required in the apm-ci anymore [#690](https://github.com/elastic/apm-pipeline-library/pull/690)
-  fix: add the mandatory tag argument [#686](https://github.com/elastic/apm-pipeline-library/pull/686)

## v1.1.130 (21/07/2020)

#### 🐛 Bug Fixes

-  fix quotes in tap2Junit [#679](https://github.com/elastic/apm-pipeline-library/pull/679)

## v1.1.129 (21/07/2020)

#### 🚀 Enhancements

-  matchesPrLabel and githubPrLabels steps [#678](https://github.com/elastic/apm-pipeline-library/pull/678)

#### ⚙️ CI

-  superLinter with junit reporting [#673](https://github.com/elastic/apm-pipeline-library/pull/673)

## v1.1.128 (17/07/2020)

#### 🐛 Bug Fixes

-  fix github pr comment [#675](https://github.com/elastic/apm-pipeline-library/pull/675)

## v1.1.127 (17/07/2020)

#### 🚀 Enhancements

-  nodeArch, is32, is64, isArm, is32arm, is64arm, is32x86, is64x86 steps [#666](https://github.com/elastic/apm-pipeline-library/pull/666)
-  add more debug traces in isCommentTrigger [#668](https://github.com/elastic/apm-pipeline-library/pull/668)

#### 🙈 No user affected

-  [test] add more tests for the gitMatchingGroup step [#671](https://github.com/elastic/apm-pipeline-library/pull/671)

## v1.1.126 (09/07/2020)

#### 🚀 Enhancements

-  installTools step with exclude [#665](https://github.com/elastic/apm-pipeline-library/pull/665)
-  approval list for pull requests [#642](https://github.com/elastic/apm-pipeline-library/pull/642)

## v1.1.125 (08/07/2020)

#### 🐛 Bug Fixes

-  fix: ignore label team:automation on changelogs [#662](https://github.com/elastic/apm-pipeline-library/pull/662)
-  fix: using capital on the refspec does not have colisions [#661](https://github.com/elastic/apm-pipeline-library/pull/661)

## v1.1.124 (08/07/2020)

#### 🚀 Enhancements

- [**team:automation**] feat: support for foce option in githubCreatePullRequest [#654](https://github.com/elastic/apm-pipeline-library/pull/654)

#### 🐛 Bug Fixes

- [**team:automation**] fix: fossa need the tools to make the init [#660](https://github.com/elastic/apm-pipeline-library/pull/660)
- [**team:automation**] fix: generate long Changelogs [#659](https://github.com/elastic/apm-pipeline-library/pull/659)

## v1.1.123 (07/07/2020)

#### 🐛 Bug Fixes

- [**team:automation**] fix: remove refspec because is no longer needed [#658](https://github.com/elastic/apm-pipeline-library/pull/658)

#### 🙈 No user affected

-  test-infra: windows7 is not provisioned [#656](https://github.com/elastic/apm-pipeline-library/pull/656)

#### ⚙️ CI

-  opbeans: dotnet enable docker generation [#657](https://github.com/elastic/apm-pipeline-library/pull/657)
-  daily: fix job name [#655](https://github.com/elastic/apm-pipeline-library/pull/655)
-  [jjbb]: add docs in the test pipeline one [#651](https://github.com/elastic/apm-pipeline-library/pull/651)

---

## v1.1.122 (03/07/2020)

#### 🚀 Enhancements

-  pre-commit: change vault secrets [#652](https://github.com/elastic/apm-pipeline-library/pull/652)
-  Pipeline for building Jenkins Heartbeat container [#645](https://github.com/elastic/apm-pipeline-library/pull/645)

#### ⚙️ CI

-  Switch to pyyaml [#647](https://github.com/elastic/apm-pipeline-library/pull/647)
-  Install yaml via pip for Heartbeat stage [#646](https://github.com/elastic/apm-pipeline-library/pull/646)

---

## v1.1.121 (02/07/2020)

#### 🚀 Enhancements

- [**/ custom**][**LGTM**] test: sample implementation of some commands [#518](https://github.com/elastic/apm-pipeline-library/pull/518)
-  feat: isBranchIndexTrigger step [#644](https://github.com/elastic/apm-pipeline-library/pull/644)

#### 🐛 Bug Fixes

-  fix: FlowInterruptedException does not return the number [#643](https://github.com/elastic/apm-pipeline-library/pull/643)

---

## v1.1.120 (01/07/2020)
*No changelog for this release.*

---

## v1.1.119 (30/06/2020)

#### 🚀 Enhancements

-  template: size to kbs format [#637](https://github.com/elastic/apm-pipeline-library/pull/637)

#### 🐛 Bug Fixes

-  [opbeans] fix the docker logs happy path [#641](https://github.com/elastic/apm-pipeline-library/pull/641)

#### 📚 Documentation

-  template: fix conditional and emoticons [#640](https://github.com/elastic/apm-pipeline-library/pull/640)
-  Update release instructions [#638](https://github.com/elastic/apm-pipeline-library/pull/638)

---

## v1.1.118 (29/06/2020)

#### 🚀 Enhancements

-  generateReport step [#634](https://github.com/elastic/apm-pipeline-library/pull/634)

---

## v1.1.117 (26/06/2020)

#### 🚀 Enhancements

-  customPRComment step [#630](https://github.com/elastic/apm-pipeline-library/pull/630)

#### 🙈 No user affected

-  test-infra: arch is supported in 5.1.0 [#632](https://github.com/elastic/apm-pipeline-library/pull/632)

---

## v1.1.116 (25/06/2020)

#### 🚀 Enhancements

-  superLinter step [#624](https://github.com/elastic/apm-pipeline-library/pull/624)
-  isInstalled step [#626](https://github.com/elastic/apm-pipeline-library/pull/626)

#### 🐛 Bug Fixes

-  fix: php report and refactor [#627](https://github.com/elastic/apm-pipeline-library/pull/627)
-  fix: fix licenseScan tests [#629](https://github.com/elastic/apm-pipeline-library/pull/629)

---

## v1.1.115 (24/06/2020)

#### 🐛 Bug Fixes

-  fix: check that docker is installed [#616](https://github.com/elastic/apm-pipeline-library/pull/616)

#### 🙈 No user affected

-  test-infra: docker in 64 bits only [#623](https://github.com/elastic/apm-pipeline-library/pull/623)
-  test-infra: support new workers [#619](https://github.com/elastic/apm-pipeline-library/pull/619)

#### ⚙️ CI

-  feat: third-party license scan several languages support [#622](https://github.com/elastic/apm-pipeline-library/pull/622)
-  [jjbb]: folder description is required [#621](https://github.com/elastic/apm-pipeline-library/pull/621)
-  [jjbb]: elastic/observability-robots [#620](https://github.com/elastic/apm-pipeline-library/pull/620)

---

## v1.1.114 (17/06/2020)

#### 🚀 Enhancements

-  tar with 7z support for windows [#614](https://github.com/elastic/apm-pipeline-library/pull/614)
-  untar step [#617](https://github.com/elastic/apm-pipeline-library/pull/617)

---

## v1.1.113 (15/06/2020)

#### 🚀 Enhancements

-  tar step supports windows [#612](https://github.com/elastic/apm-pipeline-library/pull/612)

#### 🙈 No user affected

-  test-infra: docker experimental is not enabled for MacOSX [#611](https://github.com/elastic/apm-pipeline-library/pull/611)

---

## v1.1.112 (12/06/2020)

#### 🚀 Enhancements

-  feat: withGoEnv step [#592](https://github.com/elastic/apm-pipeline-library/pull/592)

#### 🐛 Bug Fixes

-  fix duplicated GitHub PR comments in PRs with merge conflicts [#603](https://github.com/elastic/apm-pipeline-library/pull/603)

---

## v1.1.111 (12/06/2020)

#### 🚀 Enhancements

-  ci: support multiple github pr comments [#606](https://github.com/elastic/apm-pipeline-library/pull/606)
-  retryWithSleep step [#605](https://github.com/elastic/apm-pipeline-library/pull/605)
-  feat: script to process Jinja templates [#601](https://github.com/elastic/apm-pipeline-library/pull/601)

#### 🐛 Bug Fixes

-  fix the refspec as used to be [#609](https://github.com/elastic/apm-pipeline-library/pull/609)

#### 🙈 No user affected

-  test: test-data without noncps misleading traces [#607](https://github.com/elastic/apm-pipeline-library/pull/607)
-  local: use jjb docker image with 3.x jjb version [#604](https://github.com/elastic/apm-pipeline-library/pull/604)

#### ⚙️ CI

-  refactor getBuildInfoJsonFiles [#602](https://github.com/elastic/apm-pipeline-library/pull/602)

---

## v1.1.110 (10/06/2020)

#### 🚀 Enhancements

-  ci: writeVaultSecret step [#595](https://github.com/elastic/apm-pipeline-library/pull/595)
-  ci: generateBuildReport [#589](https://github.com/elastic/apm-pipeline-library/pull/589)

#### 🐛 Bug Fixes

-  PATH append system32 to support MSYSGIT [#598](https://github.com/elastic/apm-pipeline-library/pull/598)
-  fix: gitCheckout fetch should run earlier [#597](https://github.com/elastic/apm-pipeline-library/pull/597)

#### 📚 Documentation

-  docs: add more docs to the template [#585](https://github.com/elastic/apm-pipeline-library/pull/585)

#### 🙈 No user affected

-  ci: refactor gitCheckout env variables [#563](https://github.com/elastic/apm-pipeline-library/pull/563)
-  test-infra: no HOME in the Jenkinsfile [#591](https://github.com/elastic/apm-pipeline-library/pull/591)
-  test-infra: fix docker experimental issues when HOME is set [#590](https://github.com/elastic/apm-pipeline-library/pull/590)

---

## v1.1.109 (04/06/2020)

#### 🐛 Bug Fixes

-  Revert "fix: gitCheckout with customisation and unshallow in PRs (#556)" [#586](https://github.com/elastic/apm-pipeline-library/pull/586)

#### 🙈 No user affected

-  test-infra: docker experimental client side [#588](https://github.com/elastic/apm-pipeline-library/pull/588)
-  test-infra: docker experimental [#584](https://github.com/elastic/apm-pipeline-library/pull/584)

---

## v1.1.108 (04/06/2020)

#### 🚀 Enhancements

-  ci: archive GH PR comment markdown [#581](https://github.com/elastic/apm-pipeline-library/pull/581)
-  ci: exclude environments section in the docker inspect [#580](https://github.com/elastic/apm-pipeline-library/pull/580)
-  fix: run the swarm agent as vagrant user [#582](https://github.com/elastic/apm-pipeline-library/pull/582)

#### 🐛 Bug Fixes

-  ci: support retry for the apm-ui-e2e [#583](https://github.com/elastic/apm-pipeline-library/pull/583)

---

## v1.1.107 (02/06/2020)

#### 🚀 Enhancements

-  ci: cmd step [#569](https://github.com/elastic/apm-pipeline-library/pull/569)

---

## v1.1.106 (02/06/2020)

#### 🚀 Enhancements

-  ci: dockerLogs step [#570](https://github.com/elastic/apm-pipeline-library/pull/570)

#### ⚙️ CI

- [**on-hold**] jjbb: enable APM-UI pipeline for all the PRs [#562](https://github.com/elastic/apm-pipeline-library/pull/562)
-  ci: use isGitRegionMatch to run the test-infra stage [#568](https://github.com/elastic/apm-pipeline-library/pull/568)

---

## v1.1.105 (29/05/2020)

#### 🚀 Enhancements

-  ci: support windows for getGitMatchingGroup [#567](https://github.com/elastic/apm-pipeline-library/pull/567)

#### 🙈 No user affected

-  test: x-pack/oss modules in Beats [#566](https://github.com/elastic/apm-pipeline-library/pull/566)
-  ci: use master branch in our pipelines [#564](https://github.com/elastic/apm-pipeline-library/pull/564)

---

## v1.1.104 (27/05/2020)

#### 🚀 Enhancements

-  ci: isPR step [#561](https://github.com/elastic/apm-pipeline-library/pull/561)

#### 🐛 Bug Fixes

-  fix: install node for license scan [#565](https://github.com/elastic/apm-pipeline-library/pull/565)

---

## v1.1.103 (26/05/2020)

#### 🚀 Enhancements

-  feat: scan third-party licenses daily on all our repos [#555](https://github.com/elastic/apm-pipeline-library/pull/555)

#### 🐛 Bug Fixes

-  fix: gitCheckout with customisation and unshallow in PRs [#556](https://github.com/elastic/apm-pipeline-library/pull/556)
-  fix: typos on third-party dependency scan [#559](https://github.com/elastic/apm-pipeline-library/pull/559)

#### ⚙️ CI

-  ci: add forceInstall flag [#558](https://github.com/elastic/apm-pipeline-library/pull/558)
-  ci: publishToCDN step [#552](https://github.com/elastic/apm-pipeline-library/pull/552)

---

## v1.1.102 (21/05/2020)

#### 🐛 Bug Fixes

-  fix: skip preview docs with aborted builds [#553](https://github.com/elastic/apm-pipeline-library/pull/553)

#### 🙈 No user affected

-  test-infra: arch is supported in 5.1.0 [#550](https://github.com/elastic/apm-pipeline-library/pull/550)

#### ⚙️ CI

-  ci: cosmetic changes in the PR comment [#551](https://github.com/elastic/apm-pipeline-library/pull/551)

---

## v1.1.101 (19/05/2020)

#### 🐛 Bug Fixes

-  ci: fix redirect gitCmd [#549](https://github.com/elastic/apm-pipeline-library/pull/549)

#### 🙈 No user affected

-  test: add tests for beats module patterns [#545](https://github.com/elastic/apm-pipeline-library/pull/545)

#### ⚙️ CI

-  feat: generic job to scan licenses [#547](https://github.com/elastic/apm-pipeline-library/pull/547)

---

## v1.1.100 (19/05/2020)

#### 🚀 Enhancements

-  ci: less verbose git fetch [#544](https://github.com/elastic/apm-pipeline-library/pull/544)

#### 🙈 No user affected

-  [test-infra] add more configuration requirements [#543](https://github.com/elastic/apm-pipeline-library/pull/543)

---

## v1.1.99 (18/05/2020)

#### 🚀 Enhancements

-  ci: getGitMatchingGroup step [#535](https://github.com/elastic/apm-pipeline-library/pull/535)

---

## v1.1.98 (15/05/2020)

#### 🐛 Bug Fixes

-  fix: git pull only for branches [#539](https://github.com/elastic/apm-pipeline-library/pull/539)

#### 🙈 No user affected

-  test: exclude windows-2012-r2 [#537](https://github.com/elastic/apm-pipeline-library/pull/537)
-  test: fix windows7-32 labels [#536](https://github.com/elastic/apm-pipeline-library/pull/536)
-  test-infra: beats-ci improvements [#534](https://github.com/elastic/apm-pipeline-library/pull/534)

---

## v1.1.97 (14/05/2020)

#### 🚀 Enhancements

-  ci: stash/unstash steps with Google Cloud Storage [#531](https://github.com/elastic/apm-pipeline-library/pull/531)

#### 🙈 No user affected

-  Add flake detector to pipeline [#530](https://github.com/elastic/apm-pipeline-library/pull/530)
-  ci: enable beats-ci test-infra [#525](https://github.com/elastic/apm-pipeline-library/pull/525)

#### ⚙️ CI

-  Fix missing Docker login [#533](https://github.com/elastic/apm-pipeline-library/pull/533)
-  Work around git init with closure [#532](https://github.com/elastic/apm-pipeline-library/pull/532)
-  Write credentials for opbot [#529](https://github.com/elastic/apm-pipeline-library/pull/529)

---

## v1.1.96 (12/05/2020)

#### 🚀 Enhancements

-  ci: CI approval aborted rather than failed [#522](https://github.com/elastic/apm-pipeline-library/pull/522)
-  ci: generate-build-data JSON objects and no stats badge [#520](https://github.com/elastic/apm-pipeline-library/pull/520)
-  ci: notify other build status [#521](https://github.com/elastic/apm-pipeline-library/pull/521)

#### 🐛 Bug Fixes

-  ci: avoid template when no PRs [#526](https://github.com/elastic/apm-pipeline-library/pull/526)
-  fix: null build description [#528](https://github.com/elastic/apm-pipeline-library/pull/528)

#### 🙈 No user affected

-  test: move to apm-ci folder [#524](https://github.com/elastic/apm-pipeline-library/pull/524)
-  test: use a different port [#523](https://github.com/elastic/apm-pipeline-library/pull/523)
-  test: apm-ci test-infra for windows [#527](https://github.com/elastic/apm-pipeline-library/pull/527)

---

## v1.1.95 (08/05/2020)

#### ⚙️ CI

-  test: move the job to multibranch pipeline [#514](https://github.com/elastic/apm-pipeline-library/pull/514)

---

## v1.1.94 (06/05/2020)

#### 🚀 Enhancements

-  feat: licenseScan step [#515](https://github.com/elastic/apm-pipeline-library/pull/515)

---

## v1.1.93 (04/05/2020)

#### 🚀 Enhancements

-  ci: cornercase when comment has been deleted [#510](https://github.com/elastic/apm-pipeline-library/pull/510)

#### 🐛 Bug Fixes

-  fix: store comment.id [#509](https://github.com/elastic/apm-pipeline-library/pull/509)

---

## v1.1.92 (04/05/2020)

#### 🚀 Enhancements

-  ci: add error message if Error signal [#508](https://github.com/elastic/apm-pipeline-library/pull/508)
-  ci: store pr comment id only [#496](https://github.com/elastic/apm-pipeline-library/pull/496)

#### 🐛 Bug Fixes

-  ci: evaluate test results do exist [#505](https://github.com/elastic/apm-pipeline-library/pull/505)

#### 🙈 No user affected

-  chore: change error message for PR Approvals [#500](https://github.com/elastic/apm-pipeline-library/pull/500)

---

## v1.1.91 (28/04/2020)

#### 🚀 Enhancements

-  ci: support comment id as an artifact [#490](https://github.com/elastic/apm-pipeline-library/pull/490)
-  feat: filter Pipeline flow messages from the log [#489](https://github.com/elastic/apm-pipeline-library/pull/489)
-  ci(jenkins): enable PR notification [#488](https://github.com/elastic/apm-pipeline-library/pull/488)

#### 🐛 Bug Fixes

-  fix: notifyBuildResult to support big JSON files [#493](https://github.com/elastic/apm-pipeline-library/pull/493)

#### ⚙️ CI

-  ci: support preview docs in the comment [#498](https://github.com/elastic/apm-pipeline-library/pull/498)

---

## v1.1.90 (23/04/2020)

#### 🚀 Enhancements

-  refactor getBuildInfoJsonFiles: use script rather than groovy [#480](https://github.com/elastic/apm-pipeline-library/pull/480)

#### 🐛 Bug Fixes

-  ci: jq is not installed in the master [#486](https://github.com/elastic/apm-pipeline-library/pull/486)
-  fix: unshallow with unshallow [#484](https://github.com/elastic/apm-pipeline-library/pull/484)

#### 📚 Documentation

-  docs: target goal for start/stop-local-worker [#485](https://github.com/elastic/apm-pipeline-library/pull/485)

---

## v1.1.89 (22/04/2020)

#### 🚀 Enhancements

-  ci: withGitRelease step for releases [#481](https://github.com/elastic/apm-pipeline-library/pull/481)

#### 🐛 Bug Fixes

-  fix: no HOME then no global [#483](https://github.com/elastic/apm-pipeline-library/pull/483)

#### ⚙️ CI

-  ci(release): update commit user [#482](https://github.com/elastic/apm-pipeline-library/pull/482)

---

## v1.1.88 (20/04/2020)

#### 🐛 Bug Fixes

-  revert: limit cause some performance issues [#479](https://github.com/elastic/apm-pipeline-library/pull/479)
-  fix: limit URL format [#477](https://github.com/elastic/apm-pipeline-library/pull/477)

#### ⚙️ CI

-  jjbb: enable parameters for the simple pipeline [#478](https://github.com/elastic/apm-pipeline-library/pull/478)

---

## v1.1.87 (17/04/2020)

#### 🐛 Bug Fixes

-  fix: readXXX should be handled with default if no exist [#476](https://github.com/elastic/apm-pipeline-library/pull/476)
-  fix: when using limits [#475](https://github.com/elastic/apm-pipeline-library/pull/475)

---

## v1.1.86 (17/04/2020)

#### 🚀 Enhancements

-  ci: githubCreateIssue and githubCreatePullRequest steps [#468](https://github.com/elastic/apm-pipeline-library/pull/468)
-  ci(jenkins): support String[] in isGitRegionMatch [#464](https://github.com/elastic/apm-pipeline-library/pull/464)

#### 🐛 Bug Fixes

-  fix: _bulk does not validate the content [#472](https://github.com/elastic/apm-pipeline-library/pull/472)

#### ⚙️ CI

-  ci(test-infra): validate hub is installed [#467](https://github.com/elastic/apm-pipeline-library/pull/467)

---

## v1.1.85 (03/04/2020)

#### 🚀 Enhancements

-  ci(jenkins): step to configure git user.email [#462](https://github.com/elastic/apm-pipeline-library/pull/462)
-  feat: POC of a ChatOps pipeline [#461](https://github.com/elastic/apm-pipeline-library/pull/461)

#### 📚 Documentation

-  docs: fix readme format [#463](https://github.com/elastic/apm-pipeline-library/pull/463)

#### 🙈 No user affected

-  test: test base case of githubEnv in a regular pipeline. [#459](https://github.com/elastic/apm-pipeline-library/pull/459)

---

## v1.1.84 (02/04/2020)

#### 🚀 Enhancements

-  ci(jenkins): support from/to sha commits for isGitRegionMatch [#456](https://github.com/elastic/apm-pipeline-library/pull/456)
-  HTTP status code [#450](https://github.com/elastic/apm-pipeline-library/pull/450)

#### 🐛 Bug Fixes

-   fix: retry push events to docker registry [#449](https://github.com/elastic/apm-pipeline-library/pull/449)
-  fix: installTools is failing, maybe some choco issue [#447](https://github.com/elastic/apm-pipeline-library/pull/447)

#### ⚙️ CI

-  ci(jjbb): apm-ui https://github.com/elastic/kibana/pull/61803 [#451](https://github.com/elastic/apm-pipeline-library/pull/451)
-  ci: test-infra with less failures [#441](https://github.com/elastic/apm-pipeline-library/pull/441)
-  ci(jenkins): cache mvn dependencies and help to debug [#439](https://github.com/elastic/apm-pipeline-library/pull/439)

---

## v1.1.83 (17/03/2020)

#### 🐛 Bug Fixes

-  ci(jenkins): release generation without override [#437](https://github.com/elastic/apm-pipeline-library/pull/437)

---

## v1.1.82 (17/03/2020)

#### 🚀 Enhancements

-  Install Docker in image [#434](https://github.com/elastic/apm-pipeline-library/pull/434)
-  Maven version output module [#432](https://github.com/elastic/apm-pipeline-library/pull/432)
-  Add name param to github release [#427](https://github.com/elastic/apm-pipeline-library/pull/427)
-  Jenkins build status module [#433](https://github.com/elastic/apm-pipeline-library/pull/433)

#### 🐛 Bug Fixes

-  fix: renamed parameter for the ITs [#435](https://github.com/elastic/apm-pipeline-library/pull/435)
-  Remove PYTHON_EXE when building beats images [#430](https://github.com/elastic/apm-pipeline-library/pull/430)

#### ⚙️ CI

-  ci(jenkins) test-infra [#425](https://github.com/elastic/apm-pipeline-library/pull/425)

---

## v1.1.81 (06/03/2020)
*No changelog for this release.*

---

## v1.1.80 (06/03/2020)

#### ⚙️ CI

-  ci(jenkins): release stage requires the PATH [#423](https://github.com/elastic/apm-pipeline-library/pull/423)

---

## v1.1.79 (05/03/2020)

#### 🐛 Bug Fixes

-  Fix missing def in DefaultParallelTaskGenerator [#422](https://github.com/elastic/apm-pipeline-library/pull/422)

#### 🙈 No user affected

-  chore: tidy up env variables [#421](https://github.com/elastic/apm-pipeline-library/pull/421)

---

## v1.1.78 (03/03/2020)

#### 🐛 Bug Fixes

-  fix: define POSIX LANG environment variables [#417](https://github.com/elastic/apm-pipeline-library/pull/417)

#### 🙈 No user affected

-  fix: batch is not shell [#420](https://github.com/elastic/apm-pipeline-library/pull/420)

#### ⚙️ CI

-  ci(jenkins): support semantic versioning order [#419](https://github.com/elastic/apm-pipeline-library/pull/419)

---

## v1.1.77 (03/03/2020)

#### 🚀 Enhancements

-  feat: installTools step [#402](https://github.com/elastic/apm-pipeline-library/pull/402)
-  Add dumpMatrix() method [#415](https://github.com/elastic/apm-pipeline-library/pull/415)

---

## v1.1.76 (02/03/2020)

#### 🐛 Bug Fixes

-  ci(jenkins): run docker in the new home context [#416](https://github.com/elastic/apm-pipeline-library/pull/416)
-  ci(jenkins): fix cornercase with rebuild when timeouts and no githubPrCheckApproved [#412](https://github.com/elastic/apm-pipeline-library/pull/412)
-  Switch Nexus lookup to use groupid [#413](https://github.com/elastic/apm-pipeline-library/pull/413)

#### 📚 Documentation

-  docs: Add vault as a requirement for local infra [#410](https://github.com/elastic/apm-pipeline-library/pull/410)

#### ⚙️ CI

-  ci(jjbb): reuse default [#414](https://github.com/elastic/apm-pipeline-library/pull/414)

---

## v1.1.75 (27/02/2020)

#### 🐛 Bug Fixes

-  fix: if grep fails then the errors [#409](https://github.com/elastic/apm-pipeline-library/pull/409)

#### 📚 Documentation

-  Additional documentation for githubReleasePublish [#408](https://github.com/elastic/apm-pipeline-library/pull/408)

---

## v1.1.74 (26/02/2020)

#### 🚀 Enhancements

-  Automatic CHANGELOG generation [#400](https://github.com/elastic/apm-pipeline-library/pull/400)

#### 🐛 Bug Fixes

-  ci(jenkins): support more generic cases for the pre-commit [#407](https://github.com/elastic/apm-pipeline-library/pull/407)

#### 📚 Documentation

-  ci(jenkins): dummy declarative pipeline with parameters [#401](https://github.com/elastic/apm-pipeline-library/pull/401)

#### ⚙️ CI

-  ci(jenkins): generate jruby images within the apm-agent-ruby [#405](https://github.com/elastic/apm-pipeline-library/pull/405)
-  [jjbb]: enable e2e kibana tests for the master branch only [#403](https://github.com/elastic/apm-pipeline-library/pull/403)
-  [packer-cache] Cache jjbb images [#404](https://github.com/elastic/apm-pipeline-library/pull/404)
-  Add githubReleasePublish [#406](https://github.com/elastic/apm-pipeline-library/pull/406)

---

## v1.1.73 (19/02/2020)

#### 🚀 Enhancements

-  enable opbeans app key search [#397](https://github.com/elastic/apm-pipeline-library/pull/397)

---

## v1.1.72 (18/02/2020)

#### 🚀 Enhancements

-  Nexus repository integration [#385](https://github.com/elastic/apm-pipeline-library/pull/385)

#### 🐛 Bug Fixes

-  retry in the getBuildInfoJsonFiles didn't load the functions library [#393](https://github.com/elastic/apm-pipeline-library/pull/393)
-  ci(jenkins): enable .ci/scripts in other repos [#394](https://github.com/elastic/apm-pipeline-library/pull/394)

#### ⚙️ CI

- [**on-hold**] [jjbb] e2e APM UI follow-ups [#305](https://github.com/elastic/apm-pipeline-library/pull/305)

---

## v1.1.71 (17/02/2020)

#### ⚙️ CI

-  fix: getBuildInfoJsonFiles with retry 3 [#389](https://github.com/elastic/apm-pipeline-library/pull/389)
-  bump jenkins-pipeline-unit 1.3 [#391](https://github.com/elastic/apm-pipeline-library/pull/391)

---

## v1.1.70 (13/02/2020)

#### 🐛 Bug Fixes

-  fix: execute host only if exists [#386](https://github.com/elastic/apm-pipeline-library/pull/386)

#### ⚙️ CI

-  enable daily docker image generation for Node.js [#383](https://github.com/elastic/apm-pipeline-library/pull/383)
-  [cache] Build node.js docker images [#382](https://github.com/elastic/apm-pipeline-library/pull/382)

---

## v1.1.69 (10/02/2020)

#### 🚀 Enhancements

-  Support dockerLogin step in Windows [#381](https://github.com/elastic/apm-pipeline-library/pull/381)

---

## v1.1.68 (07/02/2020)

#### 🐛 Bug Fixes

-  dockerLogin step without a previous sleep causes DDoS [#380](https://github.com/elastic/apm-pipeline-library/pull/380)

---

## v1.1.67 (06/02/2020)

#### 🚀 Enhancements

-  Git release [#377](https://github.com/elastic/apm-pipeline-library/pull/377)

#### ⚙️ CI

-  sleep in the first checkout since it is the best worst scenario [#378](https://github.com/elastic/apm-pipeline-library/pull/378)

---

## v1.1.66 (05/02/2020)

#### 🚀 Enhancements

-  Speed up builds with less sleeps steps [#376](https://github.com/elastic/apm-pipeline-library/pull/376)
-  POST ability for GitHub API [#375](https://github.com/elastic/apm-pipeline-library/pull/375)

#### ⚙️ CI

-  getBuildInfoJsonFiles step refactored to be `shellish` rather than `groovish` [#373](https://github.com/elastic/apm-pipeline-library/pull/373)

---

## v1.1.65 (03/02/2020)

#### 🚀 Enhancements

-  feat: generate TOTP code with Vault and .npmrc [#367](https://github.com/elastic/apm-pipeline-library/pull/367)
-  feat: git step support sleep/retry [#370](https://github.com/elastic/apm-pipeline-library/pull/370)

#### 🐛 Bug Fixes

-  fix: support gitCheckout without a previous git repo [#371](https://github.com/elastic/apm-pipeline-library/pull/371)

#### 📚 Documentation

-  add more test coverage for the gitCheckout step [#374](https://github.com/elastic/apm-pipeline-library/pull/374)

#### ⚙️ CI

-  refactor: enum for the secrets [#368](https://github.com/elastic/apm-pipeline-library/pull/368)

---

## v1.1.64 (28/01/2020)

#### 🐛 Bug Fixes

-  fixes: build step with wait:false [#364](https://github.com/elastic/apm-pipeline-library/pull/364)
-  fixes: WorkflowScript: 145: The current scope already contains a variable [#363](https://github.com/elastic/apm-pipeline-library/pull/363)

#### ⚙️ CI

-  [cache] Build ruby and python docker images [#362](https://github.com/elastic/apm-pipeline-library/pull/362)
-  feat: build opbot daily [#361](https://github.com/elastic/apm-pipeline-library/pull/361)

---

## v1.1.63 (24/01/2020)

#### 🐛 Bug Fixes

-  fixes the foo hardcoded string for testing purposes [#360](https://github.com/elastic/apm-pipeline-library/pull/360)

---

## v1.1.62 (24/01/2020)

#### 🐛 Bug Fixes

-  fixes the isCommentTrigger step when orgs for the user are not available [#359](https://github.com/elastic/apm-pipeline-library/pull/359)

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
-  [pre-commit] Default apm-ci jenkins instance and validate ./Jenkinsfile [#281](https://github.com/elastic/apm-pipeline-library/pull/281)
-  feat: use no blacklisted methods [#275](https://github.com/elastic/apm-pipeline-library/pull/275)
-  test: set access permissions from the Jenkinsfile [#282](https://github.com/elastic/apm-pipeline-library/pull/282)
-  fix: avoid NPE on inconsistent return objects [#279](https://github.com/elastic/apm-pipeline-library/pull/279)
-  test: set permissions over a folder [#284](https://github.com/elastic/apm-pipeline-library/pull/284)

#### 🐛 Bug Fixes

-  (#213) Install mage in the build agent [#276](https://github.com/elastic/apm-pipeline-library/pull/276)
-  fix: avoid lose Exceptions [#291](https://github.com/elastic/apm-pipeline-library/pull/291)
-  (#213) Fix script path [#290](https://github.com/elastic/apm-pipeline-library/pull/290)
-  213 fix gopath [#289](https://github.com/elastic/apm-pipeline-library/pull/289)
-  (#213) Define HOME variable for ephemeral worker installing Gimme [#288](https://github.com/elastic/apm-pipeline-library/pull/288)

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

-  override build step to print the downstream URL [#259](https://github.com/elastic/apm-pipeline-library/pull/259)
-  support exact match in isGitRegionMatch [#257](https://github.com/elastic/apm-pipeline-library/pull/257)
-  bump version from 2.x support to 3.x support in order to use the filter-branch [#256](https://github.com/elastic/apm-pipeline-library/pull/256)
-  Bump jenkins core version for local testing [#247](https://github.com/elastic/apm-pipeline-library/pull/247)
-  enforce: maven dependencies and validation within the build [#246](https://github.com/elastic/apm-pipeline-library/pull/246)
-  Configure missing credentials [#244](https://github.com/elastic/apm-pipeline-library/pull/244)
-  feat: PR GitHub template [#254](https://github.com/elastic/apm-pipeline-library/pull/254)

#### 🐛 Bug Fixes

-  CRUMB issues when running the validate.sh [#241](https://github.com/elastic/apm-pipeline-library/pull/241)

#### 📚 Documentation

-  Build rotation design [#258](https://github.com/elastic/apm-pipeline-library/pull/258)

#### ⚙️ CI

-  Revert "fix: use fork to build the images" [#255](https://github.com/elastic/apm-pipeline-library/pull/255)
-  ci: bring up to date the 'update clusters' job [#243](https://github.com/elastic/apm-pipeline-library/pull/243)
-  fix: use fork to build the images [#253](https://github.com/elastic/apm-pipeline-library/pull/253)

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
-  feat: support multidocument YAML files [#210](https://github.com/elastic/apm-pipeline-library/pull/210)
-  feat: CodeCov Docker container as tool [#172](https://github.com/elastic/apm-pipeline-library/pull/172)

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

-  fix: we have to use the raw output of jq [#144](https://github.com/elastic/apm-pipeline-library/pull/144)
-  fix: grab the real version name from artifactory before push [#143](https://github.com/elastic/apm-pipeline-library/pull/143)
-  fix: make gren docker image works [#140](https://github.com/elastic/apm-pipeline-library/pull/140)
-  fix: execute .ci/scripts/push-integration-test-images.sh in the correct context [#142](https://github.com/elastic/apm-pipeline-library/pull/142)
-  fix: use alias to avoid hit erased paths [#141](https://github.com/elastic/apm-pipeline-library/pull/141)

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

-  fix: change .jenkins_python.yml file path [#106](https://github.com/elastic/apm-pipeline-library/pull/106)
-  fix: throw an error when GitHub API call fails [#110](https://github.com/elastic/apm-pipeline-library/pull/110)
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

-  [APM-CI] Provide JUnit report for checkLicenses [#77](https://github.com/elastic/apm-pipeline-library/pull/77)
-  feat: update k8s clusters from CI on Mondays [#93](https://github.com/elastic/apm-pipeline-library/pull/93)

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

-  Pr no build test [#6](https://github.com/elastic/apm-pipeline-library/pull/6)
-  [APM-CI] APM UI pipeline version 0.2 [#15](https://github.com/elastic/apm-pipeline-library/pull/15)
-  [APM-CI] APM UI pipeline version 0.1 [#14](https://github.com/elastic/apm-pipeline-library/pull/14)
-  Fix test and add new ones [#17](https://github.com/elastic/apm-pipeline-library/pull/17)
-  [APM-CI] APM UI pipeline version 0 [#4](https://github.com/elastic/apm-pipeline-library/pull/4)
-  Pr no build test [#7](https://github.com/elastic/apm-pipeline-library/pull/7)
-  Jenkinsfile [#1](https://github.com/elastic/apm-pipeline-library/pull/1)
