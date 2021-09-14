package main

import (
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
