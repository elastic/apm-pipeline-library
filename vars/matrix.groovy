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

/**
  Makes a matrix form the values of several axis and remove the asix marked for be exclude.

*/
def call(Map args = [:], Closure body) {
  def axes = args.containsKey('axes') ? args.axes : error('axes argument missing.')
  def excludes = args.get('excludes', [])
  def agent = args.get('agent')

  def listEnv = buildAxes(axes)
  def listExcludes = buildAxes(excludes)
  def tasks = [:]
  listEnv.removeAll(listExcludes)
  listEnv.each { env ->
    tasks["${env.toString()}"] = {
      withNode(agent){
        withEnv(env){
          body()
        }
      }
    }
  }
  parallel(tasks)
}

def withNode(agent, Closure body){
  if(agent){
    node(agent){
      body()
    }
  } else {
    body()
  }
}

def buildAxes(list){
    def listEnv = listToListOfList(axisToEnv(list[0]))
    for(def i=1; i<list.size(); i++){
        listEnv = addToEachListItem(listEnv, axisToEnv(list[i]))
    }
    return listEnv
}

def addToEachListItem(list, items){
    def newList = []
    list.each { axisItems ->
        items.each { newItem ->
            def newListItem = []
            newListItem.addAll(axisItems)
            newListItem.add(newItem)
            newList.add(newListItem)
        }
    }
    return newList
}

def axisToEnv(list){
    def newList = []
    list.each { item ->
        newList.add("${item.name}=${item.value}")
    }
    return newList
}

def listToListOfList(list){
    def newList = []
    list.each { item ->
        def newListItem = []
        newListItem.add(item)
        newList.add(newListItem)
    }
    return newList
}
