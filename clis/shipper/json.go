package main

import (
	"fmt"
	"io"
	"io/ioutil"

	"github.com/Jeffail/gabs/v2"
)

// ParseJSON takes a stream representing a JSON, unmarshalling it into a gabs container
func ParseJSON(stream io.ReadCloser) (*gabs.Container, error) {
	data, err := ioutil.ReadAll(stream)
	if err != nil {
		return nil, err
	}

	jsonParsed, err := gabs.ParseJSON(data)
	if err != nil {
		return nil, err
	}

	return jsonParsed, nil
}

// DeleteJSONKey deletes a key from the JSON object
func DeleteJSONKey(json *gabs.Container, key string) {
	err := json.DeleteP(key)
	if err != nil {
		fmt.Printf(">> cannot delete %s from JSON object: %s\n", key, err)
	}
}
