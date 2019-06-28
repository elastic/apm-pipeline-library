# Changelog

## v1.1.14 (28/06/2019)
*No changelog for this release.*

---

## current (28/06/2019)
- [**automation**] fix: githubNotify when success should not happen when notify is disabled [#101](https://github.com/elastic/apm-pipeline-library/pull/101)

---

## v1.1.13 (27/06/2019)
- [**automation**] ci: refactor ITs maps to be centralised and being reused if required [#95](https://github.com/elastic/apm-pipeline-library/pull/95)
- [**automation**] feat: notify first time contributor github check [#98](https://github.com/elastic/apm-pipeline-library/pull/98)

---

## v1.1.12 (27/06/2019)
- [**automation**] fix: master worker bear hug [#99](https://github.com/elastic/apm-pipeline-library/pull/99)

---

## v1.1.11 (26/06/2019)
- [**automation**][**bug**] fix: add pipeline to the job link, remove double slash from URLs [#96](https://github.com/elastic/apm-pipeline-library/pull/96)
- [**automation**] fix: notify on cleanup stage [#94](https://github.com/elastic/apm-pipeline-library/pull/94)
- [**automation**] feat: update k8s clusters from CI on Mondays [#93](https://github.com/elastic/apm-pipeline-library/pull/93)
- [**automation**] [APM-CI] Provide JUnit report for checkLicenses [#77](https://github.com/elastic/apm-pipeline-library/pull/77)

---

## v1.1.10 (24/06/2019)
- [**automation**] fix: adds protocol twice [#92](https://github.com/elastic/apm-pipeline-library/pull/92)

---

## v1.1.9 (21/06/2019)
- [**automation**][**bug**] fix: execute getVaultSecret inside a node [#91](https://github.com/elastic/apm-pipeline-library/pull/91)

---

## v1.1.8 (21/06/2019)
- [**automation**][**bug**] fix: add parameter to catchError to avoid break the build on a notification error [#89](https://github.com/elastic/apm-pipeline-library/pull/89)
- [**automation**] chore: update README about make a library release [#87](https://github.com/elastic/apm-pipeline-library/pull/87)

---

## v1.1.7 (19/06/2019)
*No changelog for this release.*

---

## v1.1.6 (19/06/2019)
- [**automation**] refactor: change credentials to access to EC [#84](https://github.com/elastic/apm-pipeline-library/pull/84)
- [**automation**] fix: change deprecated call to getVaultSecrets to the current [#85](https://github.com/elastic/apm-pipeline-library/pull/85)
- [**automation**] feat: pipeline to update test environments [#86](https://github.com/elastic/apm-pipeline-library/pull/86)
- [**closed**] (#82) Update docs about releasing a new version [#83](https://github.com/elastic/apm-pipeline-library/pull/83)

---

## v1.1.5 (14/06/2019)
- [**automation**][**bug**] (#80) Fix gitCheckout when the caller passed mergeTarget [#81](https://github.com/elastic/apm-pipeline-library/pull/81)
- [**automation**][**bug**] [APM-CI] Powershell in W2016 returns a different output [#79](https://github.com/elastic/apm-pipeline-library/pull/79)
- [**automation**] [APM-CI] checkLicenses step [#76](https://github.com/elastic/apm-pipeline-library/pull/76)

---

## v1.1.4 (12/06/2019)
- [**automation**] [APM-CI] Cosmetic changes: README and Pipeline [#75](https://github.com/elastic/apm-pipeline-library/pull/75)
- [**automation**] fix: login on the Docker Elastic registry before to push [#74](https://github.com/elastic/apm-pipeline-library/pull/74)

---

## v1.1.3 (10/06/2019)
- [**automation**] fix: avoid showing 'null' on the email subject when BRANCH_NAME is not defined [#73](https://github.com/elastic/apm-pipeline-library/pull/73)
- [**automation**] fix: check the return value of curl instead of the file is created [#72](https://github.com/elastic/apm-pipeline-library/pull/72)

---

## v1.1.2 (10/06/2019)
- [**automation**] feat: add different GitHub context for each stage to test how it behaves [#71](https://github.com/elastic/apm-pipeline-library/pull/71)
- [**automation**] Fix notifications on weird inputs [#70](https://github.com/elastic/apm-pipeline-library/pull/70)

---

## v1.1.1 (07/06/2019)
- [**automation**] fix: protect against some posibles NPE or undefined methods errors [#69](https://github.com/elastic/apm-pipeline-library/pull/69)

---

## v1.1.0 (07/06/2019)
- [**automation**]  feat: scheduled tasks [#68](https://github.com/elastic/apm-pipeline-library/pull/68)
- [**automation**] fix: protect against null values on changes fields [#67](https://github.com/elastic/apm-pipeline-library/pull/67)
- [**automation**] fix: remove job that it is in the beats-ci too [#66](https://github.com/elastic/apm-pipeline-library/pull/66)
- [**automation**] feat: add build info to the test results object [#65](https://github.com/elastic/apm-pipeline-library/pull/65)
- [**automation**] feat: new job for Integrations registry repo [#64](https://github.com/elastic/apm-pipeline-library/pull/64)
- [**automation**] fix: fix scm checkout on dockerImagesESLatest pipeline [#63](https://github.com/elastic/apm-pipeline-library/pull/63)

---

## v1.0.22 (03/06/2019)
- [**automation**] ci(jenkins): add a condition to send or not the emails [#62](https://github.com/elastic/apm-pipeline-library/pull/62)

---

## v1.0.21 (03/06/2019)
- [**closed**] ci(jenkins): new Notifications and report to Elasticsearch [#61](https://github.com/elastic/apm-pipeline-library/pull/61)

---

## v1.0.20 (03/06/2019)
- [**automation**] [APM-CI] WithGithubNotify wrapper step [#60](https://github.com/elastic/apm-pipeline-library/pull/60)

---

## v1.0.19 (31/05/2019)
- [**closed**] [APM-CI][.NET] Support .NET images [#56](https://github.com/elastic/apm-pipeline-library/pull/56)
- [**automation**] [APM-CI][All] Make the trigger by comment case-insensitive [#50](https://github.com/elastic/apm-pipeline-library/pull/50)
- [**closed**] ci(jenkins): add licenses to files, fix JJBB jobs [#59](https://github.com/elastic/apm-pipeline-library/pull/59)
- [**automation**] ci(jenkins): Update pipelines [#58](https://github.com/elastic/apm-pipeline-library/pull/58)
- [**automation**] ci(jenkins): pipelines to build/update Docker images [#57](https://github.com/elastic/apm-pipeline-library/pull/57)
- [**closed**] (#53) Include Maven wrapper into the scm [#54](https://github.com/elastic/apm-pipeline-library/pull/54)
- [**automation**] doc: update README and steps README [#55](https://github.com/elastic/apm-pipeline-library/pull/55)
- [**closed**] [ci] add files for JJBB jobs [#49](https://github.com/elastic/apm-pipeline-library/pull/49)
- [**automation**] [APM-CI][Ruby] Build Docker images for JRuby [#48](https://github.com/elastic/apm-pipeline-library/pull/48)
- [**automation**] [APM-CI] Implement a withSecretVault step [#51](https://github.com/elastic/apm-pipeline-library/pull/51)

---

## v1.0.18 (20/05/2019)
- [**automation**] Develop updates [#47](https://github.com/elastic/apm-pipeline-library/pull/47)

---

## v1.0.17 (22/04/2019)
*No changelog for this release.*

---

## v1.0.16 (11/04/2019)
*No changelog for this release.*

---

## v1.0.15 (10/04/2019)
- [**automation**] [APM-CI] use environment variables for user and password [#46](https://github.com/elastic/apm-pipeline-library/pull/46)
- [**automation**] [APM-CI] hide dockerLogin output [#45](https://github.com/elastic/apm-pipeline-library/pull/45)

---

## v1.0.14 (08/04/2019)
- [**automation**] Develop [#44](https://github.com/elastic/apm-pipeline-library/pull/44)

---

## v1.0.13 (04/04/2019)
*No changelog for this release.*

---

## v1.0.12 (01/04/2019)
- [**automation**] [APM-CI] add cache to GitHub API REST calls [#42](https://github.com/elastic/apm-pipeline-library/pull/42)
- [**automation**] [APM-CI] add token cache to the codecov step [#41](https://github.com/elastic/apm-pipeline-library/pull/41)

---

## v1.0.11 (22/03/2019)
- [**automation**] Develop [#40](https://github.com/elastic/apm-pipeline-library/pull/40)

---

## v1.0.10 (19/03/2019)
- [**automation**] [APM-CI] get commit sha before merge on PRs [#39](https://github.com/elastic/apm-pipeline-library/pull/39)

---

## v1.0.9 (01/03/2019)
*No changelog for this release.*

---

## v1.0.8 (27/02/2019)
- [**automation**] [APM-CI] test Jenkins agents capabilities [#37](https://github.com/elastic/apm-pipeline-library/pull/37)
- [**automation**] [APM-CI] Add test pipeline [#36](https://github.com/elastic/apm-pipeline-library/pull/36)

---

## v1.0.7 (26/02/2019)
- [**automation**] [APM-CI] Support bot PRs [#34](https://github.com/elastic/apm-pipeline-library/pull/34)
- [**automation**] [APM-CI] Refactor [#32](https://github.com/elastic/apm-pipeline-library/pull/32)

---

## v1.0.6 (30/01/2019)
*No changelog for this release.*

---

## v1.0.5 (23/01/2019)
- [**automation**] [APM-CI] if there are rejected reviews it fails [#29](https://github.com/elastic/apm-pipeline-library/pull/29)

---

## v1.0.4 (15/01/2019)
- [**automation**] [APM-CI] Reference repo kibana/Elasticsearch [#28](https://github.com/elastic/apm-pipeline-library/pull/28)
- [**automation**]  [APM-CI] add support to reference repository to gitCheckout [#27](https://github.com/elastic/apm-pipeline-library/pull/27)
- [**closed**] Add "flags" param to codecov [#26](https://github.com/elastic/apm-pipeline-library/pull/26)

---

## v1.0.3 (09/01/2019)
- [**closed**] [APM-CI] document the checkout process [#22](https://github.com/elastic/apm-pipeline-library/pull/22)
- [**automation**] [APM-CI] Fix Github API calls [#25](https://github.com/elastic/apm-pipeline-library/pull/25)
- [**automation**] [APM-CI] New steps httpRequest and toJSON [#23](https://github.com/elastic/apm-pipeline-library/pull/23)
- [**automation**]  allow manual build triggered always [#21](https://github.com/elastic/apm-pipeline-library/pull/21)

---

## v1.0.2 (21/12/2018)
- [**closed**] [APM-CI] APM UI pipeline version 0.2 [#15](https://github.com/elastic/apm-pipeline-library/pull/15)
- [**closed**] [APM-CI] APM UI pipeline version 0.1 [#14](https://github.com/elastic/apm-pipeline-library/pull/14)
- [**closed**] Fix test and add new ones [#17](https://github.com/elastic/apm-pipeline-library/pull/17)
- [**closed**] link on main page to the steps documentation [#18](https://github.com/elastic/apm-pipeline-library/pull/18)
- [**closed**] wrong user reference [#20](https://github.com/elastic/apm-pipeline-library/pull/20)
- [**closed**] Github REST API call error management [#19](https://github.com/elastic/apm-pipeline-library/pull/19)
- [**closed**] Check approved [#16](https://github.com/elastic/apm-pipeline-library/pull/16)
- [**closed**] [APM-CI] APM UI pipeline version 0 [#4](https://github.com/elastic/apm-pipeline-library/pull/4)
- [**closed**] step to run from inline pipelines to allow run pipelines from the library [#2](https://github.com/elastic/apm-pipeline-library/pull/2)
- [**closed**] Checkout elastic docs tools tests [#3](https://github.com/elastic/apm-pipeline-library/pull/3)
- [**closed**] Make fetch [#5](https://github.com/elastic/apm-pipeline-library/pull/5)
- [**closed**] Trim strings [#10](https://github.com/elastic/apm-pipeline-library/pull/10)
- [**closed**] GitHub env step [#13](https://github.com/elastic/apm-pipeline-library/pull/13)
- [**closed**] Delete no used steps [#12](https://github.com/elastic/apm-pipeline-library/pull/12)
- [**closed**] Update template [#11](https://github.com/elastic/apm-pipeline-library/pull/11)
- [**closed**] Jenkinsfile template [#9](https://github.com/elastic/apm-pipeline-library/pull/9)
- [**closed**] adapt Jenkinsfile to use gitCheckout step [#8](https://github.com/elastic/apm-pipeline-library/pull/8)
- [**closed**] Pr no build test [#7](https://github.com/elastic/apm-pipeline-library/pull/7)
- [**closed**] Pr no build test [#6](https://github.com/elastic/apm-pipeline-library/pull/6)
- [**closed**] Jenkinsfile [#1](https://github.com/elastic/apm-pipeline-library/pull/1)
