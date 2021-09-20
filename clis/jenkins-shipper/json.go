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
