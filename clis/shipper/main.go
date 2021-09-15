package main

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"os"
	"strconv"
	"strings"

	"github.com/Jeffail/gabs/v2"
)

const (
	BUILD_REPORT         = "build-report.json"
	PIPELINE_LOG         = "pipeline-log.txt"
	PIPELINE_LOG_SUMMARY = "pipeline-log-summary.txt"
	STEPS_INFO           = "steps-info.json"
	TESTS_ERRORS         = "tests-errors.json"
)

var baseURL string
var blueOceanBuildURL string
var blueOceanURL string
var buildResult string
var buildURL string
var durationInMillis int
var jenkinsURL string
var sharedLibPath string

func checkVariable(envVar string) string {
	value := os.Getenv(envVar)
	if strings.EqualFold(value, "") {
		fmt.Printf(">> %s should be present", envVar)
		os.Exit(1)
	}
	return value
}

func init() {
	blueOceanURL = checkVariable("BO_JOB_URL")
	blueOceanBuildURL = checkVariable("BO_BUILD_URL")
	buildResult = checkVariable("RESULT")
	duration := checkVariable("DURATION")
	buildURL = checkVariable("BUILD_URL")

	jenkinsURL = os.Getenv("JENKINS_URL")
	sharedLibPath = os.Getenv("UTILS_LIB")

	baseURL = jenkinsURL
	if strings.EqualFold(jenkinsURL, "") {
		index := strings.Index(blueOceanURL, "/blue/rest/")
		baseURL = string([]rune(blueOceanURL)[0:index])
	}

	d, err := strconv.Atoi(duration)
	if err != nil {
		fmt.Printf(">> DURATION should be present and a number: %v", durationInMillis)
		os.Exit(1)
	}
	durationInMillis = d

	fmt.Printf("============\n")
	fmt.Printf("Environment:\n")
	fmt.Printf("============\n")
	fmt.Printf("JENKINS_URL: %s\n", jenkinsURL)
	fmt.Printf("BO_JOB_URL: %s\n", blueOceanURL)
	fmt.Printf("BO_BUILD_URL: %s\n", blueOceanBuildURL)
	fmt.Printf("BUILD_URL: %s\n", buildURL)
	fmt.Printf("DURATION: %d\n", durationInMillis)
	fmt.Printf("RESULT: %s\n", buildResult)
	fmt.Printf("UTILS_LIB: %s\n", sharedLibPath)
	fmt.Printf("BASE_URL: %s\n", baseURL)
}

func main() {
	fmt.Print("Hello shipper!\n")

	err := prepareStepsInfo()
	if err != nil {
		fmt.Printf(">> %s", err)
		os.Exit(1)
	}

	err = prepareTestErrors()
	if err != nil {
		fmt.Printf(">> %s", err)
		os.Exit(1)
	}

	err = preparePipelineLog()
	if err != nil {
		fmt.Printf(">> %s", err)
		os.Exit(1)
	}

	err = prepareBuildReport()
	if err != nil {
		fmt.Printf(">> %s", err)
		os.Exit(1)
	}
}

func fetch(url string) (*gabs.Container, error) {
	fmt.Printf("INFO: curl %s\n", url)

	// Let's support retry in the CI.
	b, err := fileExists(sharedLibPath)
	if err != nil {
		return nil, err
	}

	req := HTTPRequest{
		URL: url,
	}

	times := 1

	if !strings.EqualFold(jenkinsURL, "") && b {
		times = 3
	}

	var json *gabs.Container

	for i := 0; i < times; i++ {
		json, err = GetJSON(req)
		if err != nil {
			if i == times-1 {
				return nil, err
			}

			if times > 1 {
				fmt.Printf(">> Retrying (%d)! %s\n", (times - i - 1), err)
				continue
			}
		}
		if json != nil {
			break
		}
	}

	return json, nil
}

func fetchAndDefault(url string, list bool) (*gabs.Container, error) {
	json, err := fetch(url)
	if err != nil {
		return nil, err
	}

	if json == nil {
		// valid response but empty
		if list {
			json, err = gabs.New().Array()
			if err != nil {
				return nil, err
			}
			return json, nil
		}

		return gabs.New(), nil
	}

	return json, nil
}

func fetchAndDefaultStepsInfo(url string) (*gabs.Container, error) {
	json, err := fetchAndDefault(url, false)
	if err != nil {
		return nil, err
	}

	// Prepare steps errors report
	stepsErrors := gabs.New()

	// create an array at root dir
	stepsErrors, err = stepsErrors.Array()
	if err != nil {
		return nil, err
	}

	for _, step := range json.Children() {
		if step.Path("result").Data().(string) == "FAILURE" {
			displayDescription := step.Path("displayDescription").Data()
			if step.Path("type").Data().(string) == "STEP" && (strings.EqualFold(displayDescription.(string), "") || displayDescription == nil) {
				/*
					id=$(basename "${href}")
					new=$(curl -s "${BASE_URL}${href}log/" | head -c 100)
					curlCommand "${tmp}" "${BASE_URL}${href}log/"

					Update the displayDescription for those steps with a failure and an empty displayDescription.
					For instance, when using the pipeline step `error('foo')`
					then the 'foo' message is not shown in the BlueOcean restAPI.

					lastIndex := strings.LastIndex(linksSelfHref, "/")
					id := linksSelfHref[lastIndex:]
				*/

				linksSelfHref := step.Path("_links.self.href").Data().(string)
				req := HTTPRequest{
					URL: fmt.Sprintf("%s%slog/", baseURL, linksSelfHref),
				}

				response, err := Get(req)
				/*
					If the URL was unreachable then the file won't exist.
					For such use case, then avoid any transformation.

					if [ -e "${tmp}" ] ; then
						new=$(head -c 100 "${tmp}")
						jq --arg id "${id}" --arg new "${new}" '(.[] | select(.result=="FAILURE" and .displayDescription==null and .id==$id) | .displayDescription) |= $new' "${output}" > "$tmp" && mv "$tmp" "${output}"
					fi
				*/
				if err == nil {
					defer response.Close()

					lines := []string{}
					rd := bufio.NewReader(response)
					for {
						line, err := rd.ReadString('\n')
						if err != nil {
							return nil, err
						}

						lines = append(lines, line)
						if len(lines) == 100 {
							// only retrieve 100 first elements from the log
							break
						}
					}
				}
			}

			normaliseSteps(step)

			err = stepsErrors.ArrayAppend(step)
			if err != nil {
				return nil, err
			}
		}
	}

	return stepsErrors, nil
}

func fetchAndDefaultTestsErrors(url string) (*gabs.Container, error) {
	json, err := fetchAndDefault(url, true)
	if err != nil {
		return nil, err
	}

	if json.Exists("code") && json.Path("code").Data().(int) == 404 {
		return gabs.New().Array()
	}

	for _, child := range json.Children() {
		normaliseTests(child)
	}

	return json, nil
}

func fetchAndPrepareArtifactsInfo(url string) (*gabs.Container, error) {
	json, err := fetchAndDefault(url, true)
	if err != nil {
		return nil, err
	}

	for _, child := range json.Children() {
		normaliseArtifacts(child)
	}

	return json, nil
}

func fetchAndPrepareBuildInfo(url string) (*gabs.Container, error) {
	json, err := fetchAndDefault(url, false)
	if err != nil {
		return nil, err
	}

	normaliseBuild(json)

	return json, nil
}

func fetchAndPrepareBuildReport(url string, isList bool) (*gabs.Container, error) {
	json, err := fetchAndDefault(url, isList)
	if err != nil {
		return nil, err
	}

	normaliseArtifacts(json)
	normaliseBuildReport(json)

	for _, child := range json.Children() {
		normaliseChangeset(child)
	}

	return json, nil
}

func fetchAndPrepareTestCoverageReport(url string) (*gabs.Container, error) {
	json, err := fetch(url)
	if err != nil {
		// in the case there is not code coverage report, we return an empty object
		if strings.EqualFold(err.Error(), "GET request failed with 404") {
			fmt.Printf(">> code coverage not found at %s: %v", url, err)
			return gabs.New(), nil
		}

		return nil, err
	}

	coverage := normaliseCoberturaSummary(json)
	return coverage, nil
}

func fetchAndPrepareTestsInfo(url string) (*gabs.Container, error) {
	json, err := fetchAndDefault(url, true)
	if err != nil {
		return nil, err
	}

	/*
		Tests json response differs when there were tests executed in
		the pipeline, otherwise it returns:
		{ message: "no tests", code: 404, errors: [] }
	*/
	if json.Exists("code") && json.Path("code").Data().(int) == 404 {
		return gabs.New(), nil
	}

	for _, child := range json.Children() {
		normaliseTestsWithoutStacktrace(child)
	}

	return json, nil
}

func fetchAndPrepareTestSummaryReport(url string, testJSON *gabs.Container) (*gabs.Container, error) {
	json, err := fetchAndDefault(url, true)
	if err != nil {
		// BlueOcean might return 500 in some scenarios. If so, let's parse the tests entrypoint
		if testJSON != nil {
			total := len(testJSON.Children())
			passed := 0
			failed := 0
			skipped := 0
			for _, test := range testJSON.Children() {
				status := test.Path("status").Data().(string)
				switch status {
				case "FAILED":
					failed++
				case "PASSED":
					passed++
				case "SKIPPED":
					skipped++
				}
			}

			json = gabs.New()
			json.Set(total, "total")
			json.Set(passed, "passed")
			json.Set(failed, "failed")
			json.Set(skipped, "skipped")
		}

		return nil, err
	}

	normaliseTestsSummary(json)

	return json, nil
}

// fileExists checks if a path exists in the file system
func fileExists(path string) (bool, error) {
	_, err := os.Stat(path)
	if err == nil {
		return true, nil
	}
	if os.IsNotExist(err) {
		return false, nil
	}
	return true, err
}

func normaliseArtifacts(json *gabs.Container) {
	keys := []string{"_links", "_class", "downloadable", "id", "url"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}
}

func normaliseBuild(json *gabs.Container) {
	json.Set(buildResult, "result")
	json.Set(durationInMillis, "durationInMillis")
	json.Set("FINISHED", "state")

	keys := []string{"_links", "_class", "actions", "branch", "changeSet", "pullRequest", "replayable"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}

	if json.Exists("causes") {
		causes := json.Path("causes")
		for _, cause := range causes.Children() {
			DeleteJSONKey(cause, "_class")
		}
		if len(causes.Children()) > 0 {
			// Lets flatten the causes by only getting the first entry.
			// There is two causes by default in the way CI builds run at the moment.
			json.Set(causes.Index(0), "causes")
		}
	}

	if json.Exists("artifactsZipFile") {
		// Transform relative path to absolute URL
		artifactsZipFile := json.Path("artifactsZipFile")
		json.Set(jenkinsURL+artifactsZipFile.Data().(string), "artifactsZipFile")
	}
}

func normaliseBuildReport(json *gabs.Container) {
	keys := []string{"_links", "_class", "actions", "latestRun", "permissions", "parameters"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}
}

func normaliseCoberturaSummary(json *gabs.Container) *gabs.Container {
	elements := json.Path("results.elements")

	coverage := gabs.New()

	for _, element := range elements.Children() {
		item := gabs.New()
		item.Set(element.Path("ratio").Data(), "ratio")
		item.Set(element.Path("numerator").Data(), "numerator")
		item.Set(element.Path("denominator").Data(), "denominator")

		name := element.Path("name").Data().(string)

		coverage.SetP(item, name)
	}

	return coverage
}

func normaliseChangeset(json *gabs.Container) {
	keys := []string{"author._links", "author._class"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}
}

func normaliseSteps(json *gabs.Container) {
	linksSelfHref := fmt.Sprintf("%s%slog", baseURL, json.Path("_links.self.href").Data().(string))

	json.Set(linksSelfHref, "url")

	keys := []string{"_links", "_class", "actions"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}
}

func normaliseTests(json *gabs.Container) {
	keys := []string{"_links", "_class", "state", "hasStdLog"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}

}

func normaliseTestsSummary(json *gabs.Container) {
	keys := []string{"_links", "_class"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}
}

func normaliseTestsWithoutStacktrace(json *gabs.Container) {
	normaliseTests(json)

	// This will help to tidy up the file size quite a lot.
	// It might be useful to export it but lets go step by step
	DeleteJSONKey(json, "errorStackTrace")
}

func prepareBuildReport() error {
	buildReport := gabs.New()

	job, err := fetchAndPrepareBuildReport(blueOceanURL, false)
	if err != nil {
		return err
	}
	buildReport.Set(job, "job")

	changeSet, err := fetchAndPrepareBuildReport(blueOceanBuildURL+"/changeSet/", true)
	if err != nil {
		return err
	}
	buildReport.Set(changeSet, "changeSet")

	artifacts, err := fetchAndPrepareArtifactsInfo(blueOceanBuildURL + "/artifacts/")
	if err != nil {
		return err
	}
	buildReport.Set(artifacts, "artifacts")

	testInfo, err := fetchAndPrepareTestsInfo(blueOceanBuildURL + "/tests/?limit=10000000")
	if err != nil {
		return err
	}
	buildReport.Set(testInfo, "test")

	testSummary, err := fetchAndPrepareTestSummaryReport(blueOceanBuildURL+"/blueTestSummary/?limit=10000000", testInfo)
	if err != nil {
		return err
	}
	buildReport.Set(testSummary, "test_summary")

	cobertura, err := fetchAndPrepareTestCoverageReport(buildURL + "/cobertura/api/json?tree=results[elements[name,ratio,denominator,numerator]]&depth=3")
	if err != nil {
		return err
	}
	buildReport.Set(cobertura, "test_coverage")

	buildInfo, err := fetchAndPrepareBuildInfo(blueOceanBuildURL)
	if err != nil {
		return err
	}
	buildReport.Set(buildInfo, "build")

	ioutil.WriteFile(BUILD_REPORT, []byte(buildReport.String()), 0644)

	return nil
}

func preparePipelineLog() error {
	url := fmt.Sprintf("%s/log/", blueOceanBuildURL)
	req := HTTPRequest{
		URL: url,
	}
	pipelineLog, err := GetStringArray(req, -1)
	if err != nil {
		return err
	}

	fullPipelineLog := ""
	summaryPipelineLog := ""

	// avoid a second call to retrieve the log iterating through the entire log

	// get summary
	for i := 0; i < 100; i++ {
		line := pipelineLog[i]
		fullPipelineLog += line

		// remove useless log entries from the summary
		if !strings.Contains(line, "[Pipeline]") {
			summaryPipelineLog += line
		}
	}
	// get rest of the pipeline log
	for i := 100; i < len(pipelineLog); i++ {
		fullPipelineLog += pipelineLog[i]
	}

	ioutil.WriteFile(PIPELINE_LOG, []byte(fullPipelineLog), 0644)
	ioutil.WriteFile(PIPELINE_LOG_SUMMARY, []byte(summaryPipelineLog), 0644)

	return nil
}

func prepareStepsInfo() error {
	url := fmt.Sprintf("%s/steps/?limit=10000", blueOceanBuildURL)

	steps, err := fetchAndDefaultStepsInfo(url)
	if err != nil {
		return err
	}

	ioutil.WriteFile(STEPS_INFO, []byte(steps.String()), 0644)

	return nil
}

func prepareTestErrors() error {
	url := fmt.Sprintf("%s/tests/?status=FAILED", blueOceanBuildURL)

	testErrors, err := fetchAndDefaultTestsErrors(url)
	if err != nil {
		return err
	}

	ioutil.WriteFile(TESTS_ERRORS, []byte(testErrors.String()), 0644)

	return nil
}
