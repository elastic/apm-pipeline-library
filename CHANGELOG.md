# Changelog

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

-  Add "flags" param to codecov [#26](https://github.com/elastic/apm-pipeline-library/pull/26)
-   [APM-CI] add support to reference repository to gitCheckout [#27](https://github.com/elastic/apm-pipeline-library/pull/27)

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

-  Fix test and add new ones [#17](https://github.com/elastic/apm-pipeline-library/pull/17)
-  [APM-CI] APM UI pipeline version 0 [#4](https://github.com/elastic/apm-pipeline-library/pull/4)
-  Pr no build test [#7](https://github.com/elastic/apm-pipeline-library/pull/7)
-  Jenkinsfile [#1](https://github.com/elastic/apm-pipeline-library/pull/1)
-  Pr no build test [#6](https://github.com/elastic/apm-pipeline-library/pull/6)
-  [APM-CI] APM UI pipeline version 0.2 [#15](https://github.com/elastic/apm-pipeline-library/pull/15)
-  [APM-CI] APM UI pipeline version 0.1 [#14](https://github.com/elastic/apm-pipeline-library/pull/14)
