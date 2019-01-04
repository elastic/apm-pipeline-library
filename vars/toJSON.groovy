import net.sf.json.JSONSerializer

/**
  This step converts a JSON string to net.sf.json.JSON or and POJO to net.sf.json.JSON.
  readJSON show the JSON in the Blue Ocean console output so it can not be used.
  https://issues.jenkins-ci.org/browse/JENKINS-54248
  
  net.sf.json.JSON obj = toJSON("{property: value, property1: value}")
  
  Person p = new Person();
  p.setName("John");
  p.setAge(50);
  net.sf.json.JSONObject obj = toJSON(p)
*/

def call(value){
  def obj = null
  if(value != null){
    try {
      obj = JSONSerializer.toJSON(value);
    } catch(e){
      //NOOP
      log(level: 'DEBUG', text: "toJSON: Unable to Parsing JSON: ${e?.message}" )
    }
  }
  return obj
}

