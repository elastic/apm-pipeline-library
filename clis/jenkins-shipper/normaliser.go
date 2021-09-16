package main

import (
	"fmt"

	"github.com/Jeffail/gabs/v2"
)

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
