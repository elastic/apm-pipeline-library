#!/usr/bin/env bash
# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

#
# For the Jenkins step and optional docker compose file
# then query all the docker containers and export their
# docker logs as individual files.
#
# This script download a list of .NET SKDs from the original source.
#
curl -s -O -L https://dotnet.microsoft.com/download/dotnet-core/scripts/v1/dotnet-install.sh

chmod ugo+rx dotnet-install.sh

echo "" > urls.txt

for v in '2.1.505' '3.0.103' '3.1.100' '5.0.203'
do
 ./dotnet-install.sh  -version "${v}" --architecture "x64" --os "linux" --dry-run |grep URL|grep linux|cut -d ":" -f 3-|tr -d " " >> urls.txt
done

grep -v '^\s*$' < urls.txt | while IFS= read -r i
do
  echo "Downloading $i"
  curl -sSLO "$i"
done
