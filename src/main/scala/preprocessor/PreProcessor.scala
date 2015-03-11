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
trait PreProcessor {
  /**
   * This trait is for processorhub
   * For preprocessing the input before feeding the corpus to the model
   * */
  def parse(s:String):String
}
