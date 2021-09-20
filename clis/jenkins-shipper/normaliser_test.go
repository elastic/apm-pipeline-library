package main

import (
	"io/ioutil"
	"path/filepath"
	"testing"

	"github.com/Jeffail/gabs/v2"
	"github.com/stretchr/testify/assert"
)

var resourcesPath = []string{"..", "..", "src", "test", "resources"}

func TestNormaliseArtifacts(t *testing.T) {
	artifacts := getJSONFile(t, "artifacts-info.json")

	keys := []string{"_links", "_class", "downloadable", "id", "url"}
	for _, artifact := range artifacts.Children() {
		normaliseArtifacts(artifact)

		for _, key := range keys {
			assert.False(t, artifact.Exists(key), "key should not be present after normalisation")
		}
	}
}

func TestNormaliseBuild(t *testing.T) {
	buildInfo := getJSONFile(t, "build-info.json")

	keys := []string{"_links", "_class", "actions", "branch", "changeSet", "pullRequest", "replayable"}

	normaliseBuild(buildInfo)

	assert.True(t, buildInfo.Exists("durationInMillis"), "key should be present after normalisation")
	assert.True(t, buildInfo.Exists("result"), "key should be present after normalisation")
	assert.True(t, buildInfo.Exists("state"), "key should be present after normalisation")

	for _, key := range keys {
		assert.False(t, buildInfo.Exists(key), "key should not be present after normalisation")
	}
}

func TestNormaliseBuild_ShouldHaveOneCause(t *testing.T) {
	buildInfo := getJSONFile(t, "build-info.json")

	causes := buildInfo.Path("causes")
	shortDescription := causes.Index(0).Path("shortDescription").Data().(string)

	normaliseBuild(buildInfo)

	causes = buildInfo.Path("causes")
	// was converted from an array to an object
	assert.Equal(t, shortDescription, causes.Path("shortDescription").Data())
	// sample file is nil
	assert.Nil(t, buildInfo.Path("artifactsZipFile").Data())
}

func TestNormaliseBuildReport(t *testing.T) {
	job := getJSONFile(t, "build-report.json").Path("job")

	keys := []string{"_links", "_class", "actions", "latestRun", "permissions", "parameters"}
	normaliseBuildReport(job)

	for _, key := range keys {
		assert.False(t, job.Exists(key), "key should not be present after normalisation")
	}
}

func TestNormaliseCoberturaSummary(t *testing.T) {
	json, err := gabs.ParseJSON([]byte(`{
		"_class":"hudson.plugins.cobertura.targets.CoverageResult",
		"results":{
			"elements":[
				{"denominator":61.0,"name":"Packages","numerator":61.0,"ratio":100.0},
				{"denominator":159.0,"name":"Files","numerator":159.0,"ratio":100.0},
				{"denominator":340.0,"name":"Classes","numerator":329.0,"ratio":96.76471},
				{"denominator":1003.0,"name":"Methods","numerator":915.0,"ratio":91.22632},
				{"denominator":12417.0,"name":"Lines","numerator":10300.0,"ratio":82.95079},
				{"denominator":0.0,"name":"Conditionals","numerator":0.0,"ratio":100.0}
			]
		}
	}`))
	if err != nil {
		t.Errorf("could not parse JSON from string")
	}

	cobertura := normaliseCoberturaSummary(json)

	names := []string{"Packages", "Files", "Classes", "Methods", "Lines", "Conditionals"}
	keys := []string{"ratio", "numerator", "denominator"}

	for _, name := range names {
		assert.True(t, cobertura.Exists(name), "%s should be present after normalisation", name)
		nameJSON := cobertura.Path(name)
		for _, key := range keys {
			assert.True(t, nameJSON.Exists(key), "%s - %s should be present after normalisation", name, key)
		}
	}

}

func TestNormaliseChangeSet(t *testing.T) {
	changeSets, err := gabs.ParseJSON([]byte(`[
		{
		  "affectedPaths": [
			"pipeline.yml"
		  ],
		  "author": {
			"avatar": null,
			"_links": [ ],
			"_class": "co.elastic",
			"email": null,
			"fullName": "Lola Flores",
			"id": "Lola.Flores",
			"permission": null
		  },
		  "checkoutCount": 0,
		  "commitId": "abcdefg",
		  "issues": [ ],
		  "msg": "indicator type url is in upper case (#1234)",
		  "timestamp": "2021-02-22T10:23:51.000+0000"
		}
	  ]`))
	if err != nil {
		t.Errorf("could not parse JSON from string")
	}

	keys := []string{"author._links", "author._class"}
	for _, changeSet := range changeSets.Children() {
		normaliseChangeset(changeSets)

		for _, key := range keys {
			assert.False(t, changeSet.Exists(key), "key should not be present after normalisation")
		}
	}
}

func TestNormaliseSteps_RemoveKeys(t *testing.T) {
	job := getJSONFile(t, "build-report.json").Path("job")

	normaliseSteps("http://example.com", job)

	keys := []string{"_links", "_class", "actions"}
	for _, key := range keys {
		assert.False(t, job.Exists(key), "key should not be present after normalisation")
	}
}

func TestNormaliseSteps_UpdatesUrlFromLinks(t *testing.T) {
	job := getJSONFile(t, "build-report.json").Path("job")

	normaliseSteps("http://example.com", job)

	assert.True(t, job.Exists("url"), "key should be present after normalisation")
	// _links.self.href
	assert.Equal(t, "http://example.com/blue/rest/organizations/jenkins/pipelines/apm-shared/pipelines/apm-apm-pipeline-library-mbp/pipelines/develop/log", job.Path("url").Data().(string))
}

func TestNormaliseTests(t *testing.T) {
	testsInfo := getJSONFile(t, "tests-info.json")

	keys := []string{"_links", "_class", "state", "hasStdLog"}
	for _, testInfo := range testsInfo.Children() {
		normaliseTests(testInfo)

		for _, key := range keys {
			assert.False(t, testInfo.Exists(key), "key should not be present after normalisation")
		}
	}
}

func TestNormaliseTestsSummary(t *testing.T) {
	testsInfo := getJSONFile(t, "tests-summary.json")

	keys := []string{"_links", "_class"}
	for _, testInfo := range testsInfo.Children() {
		normaliseTestsSummary(testInfo)

		for _, key := range keys {
			assert.False(t, testInfo.Exists(key), "key should not be present after normalisation")
		}
	}
}

func TestNormaliseTestsWithoutStackTrace(t *testing.T) {
	testsInfo := getJSONFile(t, "tests-info.json")

	keys := []string{"_links", "_class", "state", "hasStdLog", "errorStackTrace"}
	for _, testInfo := range testsInfo.Children() {
		normaliseTestsWithoutStacktrace(testInfo)

		for _, key := range keys {
			assert.False(t, testInfo.Exists(key), "key should not be present after normalisation")
		}
	}
}

func getJSONFile(t *testing.T, p string) *gabs.Container {
	pFilePath := append(resourcesPath, p)

	bytes, err := ioutil.ReadFile(filepath.Join(pFilePath...))
	if err != nil {
		t.Errorf("could not open test resource")
	}

	json, err := gabs.ParseJSON(bytes)
	if err != nil {
		t.Errorf("could not parse test resource into JSON")
	}

	return json
}
