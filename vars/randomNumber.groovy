import java.util.Random;

/**
  def i = randomNumber()
  def i = randomNumber(min: 1, max: 99)
*/
def call(Map params = [:]){
  def max = params.containsKey('max') ? params.max : 99
  def min = params.containsKey('min') ? params.min : 1

  Random rand = new Random();
  return rand.nextInt(max) + min;
}
