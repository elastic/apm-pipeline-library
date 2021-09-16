package main

import (
	"fmt"

	"github.com/Jeffail/gabs/v2"
)

// Get executes a GET request returning a JSON, unmarshalling it into a gabs container
func GetJSON(r HTTPRequest) (*gabs.Container, error) {
	r.method = "GET"

	stream, err := Get(r)
	if err != nil {
		return nil, err
	}
	defer stream.Close()

	jsonParsed, err := gabs.ParseJSONBuffer(stream)
	if err != nil {
		return nil, err
	}

	return jsonParsed, nil
}

// DeleteJSONKey deletes a key from the JSON object
func DeleteJSONKey(json *gabs.Container, key string) {
	if !json.ExistsP(key) {
		return
	}

	err := json.DeleteP(key)
	if err != nil {
		fmt.Printf(">> cannot delete %s from JSON object: %s\n", key, err)
	}
}
