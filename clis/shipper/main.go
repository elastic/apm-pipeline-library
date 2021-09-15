package main

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"os"
	"strings"

	"github.com/Jeffail/gabs/v2"
)

const (
	PIPELINE_LOG = "pipeline-log.txt"
	STEPS_ERRORS = "steps-errors.json"
	STEPS_INFO   = "steps-info.json"
	TESTS_ERRORS = "tests-errors.json"
)

var baseURL string
var blueOceanURL string
var jenkinsURL string
var sharedLibPath string

func init() {
	jenkinsURL = os.Getenv("JENKINS_URL")
	blueOceanURL = os.Getenv("BO_JOB_URL")
	sharedLibPath = os.Getenv("UTILS_LIB")

	baseURL = jenkinsURL
	if strings.EqualFold(jenkinsURL, "") {
		index := strings.Index(blueOceanURL, "/blue/rest/")
		baseURL = string([]rune(blueOceanURL)[0:index])
	}

	fmt.Printf("JENKINS_URL: %s\n", jenkinsURL)
	fmt.Printf("BO_JOB_URL: %s\n", blueOceanURL)
	fmt.Printf("UTILS_LIB: %s\n", sharedLibPath)
	fmt.Printf("BASE_URL: %s\n", baseURL)
}

func main() {
	fmt.Print("Hello shipper!\n")

	url := fmt.Sprintf("%s/steps/?limit=10000", blueOceanURL)

	steps, err := fetchAndDefaultStepsInfo(url)
	if err != nil {
		fmt.Printf(">> %s", err)
		os.Exit(1)
	}
	ioutil.WriteFile(STEPS_INFO, []byte(steps.String()), 0644)

	url = fmt.Sprintf("%s/tests/?status=FAILED", blueOceanURL)
	testErrors, err := fetchAndDefaultTestsErrors(url)
	if err != nil {
		fmt.Printf(">> %s", err)
		os.Exit(1)
	}
	ioutil.WriteFile(TESTS_ERRORS, []byte(testErrors.String()), 0644)

	url = fmt.Sprintf("%s/log/", blueOceanURL)
	req := HTTPRequest{
		URL: url,
	}
	pipelineLog, err := GetString(req)
	if err != nil {
		fmt.Printf(">> %s", err)
		os.Exit(1)
	}
	ioutil.WriteFile(PIPELINE_LOG, []byte(pipelineLog), 0644)
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
				fmt.Printf(">> Retrying (%d)! %s\n", (times - i), err)
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

	children := json.Children()
	if len(children) == 0 {
		normaliseSteps(json)
		return json, nil
	}

	for _, child := range children {
		normaliseTests(child)
	}

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

func normaliseTestsWithoutStacktrace(json *gabs.Container) {
	normaliseTests(json)

	DeleteJSONKey(json, "errorStackTrace")
}
