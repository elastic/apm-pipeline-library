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
  /** Name to be displayed in the UI/logs */
  public String name
  /** object to access to pipeline steps */
  public steps

  public DefaultParallelTaskGenerator(Map params){
    this.name = params.name
    this.tag = params.tag
    this.xKey = params.xKey
    this.yKey = params.yKey
    this.xFile = params.xFile
    this.yFile = params.yFile
    this.exclusionFile = params.exclusionFile
    this.steps = params.steps
  }
  /**
    read the X versions YAML file and return a versions list.
  */
  protected Map getXVersions(){
    return steps.readYaml(file: this.xFile)[this.xKey]
  }

  /**
    read the Y versions YAML file and return a versions list.
  */
  protected Map getYVersions(){
    return steps.readYaml(file: this.yFile)[this.yKey]
  }

  /**
    read the excludes YAML file and return a map of version pairs to exclude.
  */
  protected List getExcludeVersions(){
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
        column[key] = [X: x, Y: y]
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
    results.x = getXVersions()
    results.y = getYVersions()
    results.excludes = getExcludeVersions()
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
      def keyGrp = "${this.tag}-${value.X}-${value.Y}"
      parallelStep[keyGrp] = generateStep(value.X, value.Y)
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
