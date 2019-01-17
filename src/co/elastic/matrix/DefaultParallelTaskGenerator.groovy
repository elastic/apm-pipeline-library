package co.elastic

/**
  Base class to generate parallel tasks from a series of YAML files.
*/
class DefaultParallelTaskGenerator {
  /** Attribute to store results */
  public Map results = [:]
  /** Tag to identify the tasks */
  public String tag
  /* versions to use for the 'x' coordinates */
  public Map xVersions
  /* versions to use for the 'y' coordinates */
  public Map yVersions
  /* versions to exclude for the pairs 'x,y' */
  public Map excludedVersions
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
    if(params.xFile){
      this.xVersions = loadXVersions(params.xFile, params.xKey)
    } else {
      xVersions = params.xVersions
    }
    if(params.yFile){
      this.yVersions = loadYVersion(params.yFile, params.yKey)
    } else {
      yVersions = params.yVersions
    }
    if(params.exclusionFile){
      this.excludedVersions = loadExcludeVersions(params.exclusionFile, params.xKey, params.yKey)
    } else {
      this.excludedVersions = params.excludedVersions
    }
  }
  /**
    read the X versions YAML file and return a versions list.
  */
  protected List loadXVersions(xFile, xKey){
    return steps.readYaml(file: xFile)[xKey]
  }

  /**
    read the Y versions YAML file and return a versions list.
  */
  protected List loadYVersions(yFile, yKey){
    return steps.readYaml(file: yFile)[yKey]
  }

  /**
    read the excludes YAML file and return a map of version pairs to exclude.
  */
  protected List loadExcludeVersions(exclusionFile, xKey, yKey){
    def ret = []
    steps.readYaml(file: exclusionFile)['exclude'].each{ v ->
      def x = v[xKey]
      def y = v[yKey]
      String key = "${x}#${y}"
      steps.log(level: "DEBUG", text: "Exclude : ${key}")
      ret.add(key)
    }
    return ret
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
    results.data = [:]
    results.tag = this.tag
    results.name = this.name
    results.x = this.xVersions
    results.y = this.yVersions
    results.excludes = this.excludedVersions
    return buildMatrix();
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
      def keyGrp = "${this.tag}-${value.x}-${value.y}"
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
