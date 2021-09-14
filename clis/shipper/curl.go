// Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
// or more contributor license agreements. Licensed under the Elastic License;
// you may not use this file except in compliance with the Elastic License.

package main

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"net/url"
)

// HTTPRequest configures an HTTP request
type HTTPRequest struct {
	BasicAuthUser     string
	BasicAuthPassword string
	EncodeURL         bool
	Headers           map[string]string
	method            string
	Payload           string // string representation of fthe payload, in JSON format
	QueryString       string
	URL               string
}

// GetURL returns the URL as a string
func (req *HTTPRequest) GetURL() string {
	if req.QueryString == "" {
		return req.URL
	}

	u := req.URL + "?"
	if req.EncodeURL {
		return u + url.QueryEscape(req.QueryString)
	}

	return u + req.QueryString
}

// Get executes a GET request
func Get(r HTTPRequest) (io.ReadCloser, error) {
	r.method = "GET"

	return request(r)
}

// Post executes a POST request
func Post(r HTTPRequest) (io.ReadCloser, error) {
	r.method = "POST"

	return request(r)
}

// Post executes a request
func request(r HTTPRequest) (io.ReadCloser, error) {
	escapedURL := r.GetURL()

	var body io.Reader
	if r.Payload != "" {
		body = bytes.NewReader([]byte(r.Payload))
	} else {
		body = nil
	}

	req, err := http.NewRequest(r.method, escapedURL, body)
	if err != nil {
		return nil, err
	}

	if r.Headers != nil {
		for k, v := range r.Headers {
			req.Header.Set(k, v)
		}
	}

	if r.BasicAuthUser != "" {
		req.SetBasicAuth(r.BasicAuthUser, r.BasicAuthPassword)
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, err
	}
	// responsibility of the consumer
	//defer resp.Body.Close()

	// http.Status ==> [2xx, 4xx)
	if resp.StatusCode >= http.StatusOK && resp.StatusCode < http.StatusBadRequest {
		return resp.Body, nil
	}

	return nil, fmt.Errorf("%s request failed with %d", r.method, resp.StatusCode)
}
