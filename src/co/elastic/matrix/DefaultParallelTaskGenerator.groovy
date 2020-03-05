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

package co.elastic

/**
  Base class to generate parallel tasks from a series of YAML files.
*/
class DefaultParallelTaskGenerator {
  /** Attribute to store results */
  public Map results = [:]
  /** Tag to identify the tasks */
  public String tag
  /** YAML file 'x' coordinates Key */
  public String xKey
  /** YAML file 'y' coordinates Key */
  public String yKey
  /** YAML file 'x' coordinates path */
  public String xFile
  /** YAML file 'y' coordinates path */
  public String yFile
  /** YAML file exclusions path */
  public String exclusionFile
  /* versions to use for the 'x' coordinates */
  public List xVersions
  /* versions to use for the 'y' coordinates */
  public List yVersions
  /* versions to exclude for the pairs 'x,y' */
  public List excludedVersions
  /** Name to be displayed in the UI/logs */
  public String name
  /** object to access to pipeline steps */
  public steps

  /**
    DefaultParallelTaskGenerator(
      name: 'Name',
      tag: 'ID',
      xFile: 'xfile.yml',
      xkey: 'X_VERSION',
      yFile: 'yfile.yml',
      ykey: 'Y_VERSION',
      exclusionFile: 'exclusion_file.yml'
    )

    DefaultParallelTaskGenerator(
      name: 'Name',
      tag: 'ID',
      xVersions: [ '1.0', '2.0' ],
      yVersions: [ 'b1.0', 'b2.0' ],
      excludedVersions: [ '2.0#b1.0', '1.0#b2.0' ]
    )
  */
  public DefaultParallelTaskGenerator(Map params){
    this.name = params.name
    this.tag = params.tag
    this.steps = params.steps
    this.xVersions = params.xVersions
    this.yVersions = params.yVersions
    this.excludedVersions = params.excludedVersions
    this.xKey = params.xKey
    this.yKey = params.yKey
    this.xFile = params.xFile
    this.yFile = params.yFile
    this.exclusionFile = params.exclusionFile
  }
  /**
    read the X versions YAML file and return a versions list.
  */
  protected List loadXVersions(){
    if(this.xVersions){
      return this.xVersions
    } else {
      return steps.readYaml(file: this.xFile)[this.xKey]
    }
  }

  /**
    read the Y versions YAML file and return a versions list.
  */
  protected List loadYVersions(){
    if(this.yVersions){
      return this.yVersions
    } else {
      return steps.readYaml(file: this.yFile)[this.yKey]
    }
  }

  /**
    read the excludes YAML file and return a map of version pairs to exclude.
  */
  protected List loadExcludeVersions(){
    if(this.excludedVersions){
      return this.excludedVersions
    } else {
      def ret = []
      steps.readYaml(file: this.exclusionFile)['exclude'].each{ v ->
        def x = v[this.xKey]
        def y = v[this.yKey]
        String key = "${x}#${y}"
        steps.log(level: "DEBUG", text: "Exclude : ${key}")
        ret.add(key)
      }
      return ret
    }
  }

  /**
    save a test exit record to the results variable.
  */
  protected saveResult(x, y, value){
    if(results.data[x] == null){
      results.data[x] = [:]
      results.data[x]["${results.name}"] = x
    }
    results.data[x][y] = value
  }

  /**
    build a map pairs of a x value with every 'y' value, and remove the excludes.
  */
  protected Map buildColumn(x, yItems, excludes){
    def column = [:]
    yItems.each{ y ->
      String key = "${x}#${y}"
      if(!excludes.contains(key)){
        column[key] = [x: x, y: y]
      }
    }
    return column
  }

  /**
    Initialize the results map, read the info from the YAML files, and call the
    buildMatrix method to build the parallel tasks.
  */
  public Map generateParallelTests() {
    results = [:]
    results.build = [:]
    results.build.fullName = this.steps.env.JOB_NAME.replace("/","%2F")
    results.build.id = this.steps.env.BUILD_NUMBER
    results.data = [:]
    results.tag = this.tag
    results.name = this.name
    results.x = loadXVersions()
    results.y = loadYVersions()
    results.excludes = loadExcludeVersions()
    return buildMatrix();
  }
  /**
    Dump matrix by building a list where each entry is a string
    that has the X value and the Y value separated by each other
    by a marker. For example, where X is "foo" and Y is "bar" and
    the marker is "-", the entry in the list would by "foo-bar".

    If tests have not already been generated via generateParallelTests()
    this method will raise an exception
  */
  public List dumpMatrix(marker){
    if(results.size() == 0){
      error("Must generate tests first. Did you run generateParallelTests()?")
    } else {
      def dump = []
      results.x.each{ x ->
        results.y.each{ y ->
          String key = "${x}#${y}"
          if(!results.excludes.contains(key)){
            dump.add("${x}${marker}${y}")
          }
        }
      }
      return dump
    }
  }

  /**
    build the x,y pairs, remove the excludes and call the method
    to build the parallel task for each pair.
  */
  protected Map buildMatrix(){
    def parallelSteps = [:]
    results.x.each{ x ->
      def column = buildColumn(x, results.y, results.excludes)
      def stagesMap = generateParallelSteps(column)
      parallelSteps.putAll(stagesMap)
    }
    steps.log(level: "DEBUG", text: "parallelStages : ${parallelSteps.toString()}")
    return parallelSteps
  }

  /**
    build a map of closures to be used as parallel steps.
    This method can be overwritten by the target pipeline.
  */
  protected Map generateParallelSteps(column){
    def parallelStep = [:]
    column.each{ key, value ->
      def keyGrp = "${this.tag}:${value.x}#${value.y}"
      parallelStep[keyGrp] = generateStep(value.x, value.y)
    }
    return parallelStep
  }

  /**
    Default step implementation, it will provision a node and echo some text.
    This method should be overwritten by the target pipeline.
  */
  protected Closure generateStep(x, y){
    return {
      steps.node('linux && immutable'){
        try {
          steps.echo "${tag} - ${x} - ${y}"
          saveResult(x, y, 1)
        } catch(e){
          saveResult(x, y, 0)
          steps.error("Some ${tag} tests failed")
        } finally {
          steps.junit(
            allowEmptyResults: false,
            keepLongStdio: true,
            testResults: "**/tests/results/*-junit*.xml")
        }
      }
    }
  }
}
