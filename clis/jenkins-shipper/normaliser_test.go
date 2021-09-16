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
