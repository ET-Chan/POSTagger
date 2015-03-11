/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package preprocessor

/**
 * Created by et on 26/02/15.
 */
class LowerCaseProcessor extends PreProcessor{
  /**
   * Transform all the words to lowercase
   * */
  override def parse(s: String): String = {
    s.toLowerCase
  }
}
