// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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

func normaliseBuild(prefix string, json *gabs.Container) {
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
			json.Set(causes.Index(0).Data(), "causes")
		}
	}

	if json.Exists("artifactsZipFile") {
		// Transform relative path to absolute URL
		artifactsZipFile := json.Path("artifactsZipFile")
		if artifactsZipFile.Data() != nil {
			json.Set(prefix+artifactsZipFile.Data().(string), "artifactsZipFile")
		}
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
		name := element.Path("name").Data().(string)

		coverage.SetP(element.Path("ratio").Data(), name+".ratio")
		coverage.SetP(element.Path("numerator").Data(), name+".numerator")
		coverage.SetP(element.Path("denominator").Data(), name+".denominator")
	}

	return coverage
}

func normaliseChangeset(json *gabs.Container) {
	keys := []string{"author._links", "author._class"}
	for _, key := range keys {
		DeleteJSONKey(json, key)
	}
}

func normaliseSteps(prefix string, json *gabs.Container) {
	linksSelfHref := fmt.Sprintf("%s%slog", prefix, json.Path("_links.self.href").Data().(string))

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
