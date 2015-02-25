/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package misc

import Utils._

class NumberProcessor {

  /**
   * This class is used for transfer string with digits to its uniform representation
   * i.e. 114.332 will be transformed as NUMBER.NUMBER
   * 4/23 will become NUMBER/NUMBER
   * 24934958 will become NUMBER
   * to capture the invariance among these strings
   */
  val digitR = """([\d]+)""".r //Notice that it is greedy matching,
                               //otherwise it will give us something like
                               //NUMBERNUMBER.NUMBERNUMBER if we input 11.32
  def parse(s:String):String={
    digitR.replaceAllIn(s,NUMBERSTR)
  }
}
