/**
 Grab the coverage files, and create the report in Jenkins.

 coverageReport("${WORKSPACE}/build")

*/

def call(baseDir) {
  publishHTML(target: [
    allowMissing: true,
    keepAll: true,
    reportDir: "${baseDir}",
    reportFiles: 'coverage-*-report.html',
    reportName: 'Coverage-Sourcecode-Files',
    reportTitles: 'Coverage'])
/*
  The current version does not show the coverage on the source code.
  publishCoverage(adapters: [
    coberturaAdapter("${baseDir}/coverage-*-report.xml")],
    sourceFileResolver: sourceFiles('STORE_ALL_BUILD'))
*/
  cobertura(autoUpdateHealth: false,
    autoUpdateStability: false,
    coberturaReportFile: "${baseDir}/coverage-*-report.xml",
    conditionalCoverageTargets: '70, 0, 0',
    failNoReports: false,
    failUnhealthy: false,
    failUnstable: false,
    lineCoverageTargets: '80, 0, 0',
    maxNumberOfBuilds: 0,
    methodCoverageTargets: '80, 0, 0',
    onlyStable: false,
    sourceEncoding: 'ASCII',
    zoomCoverageChart: false)
}
