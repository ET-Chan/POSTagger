/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package preprocessor

import scala.collection.mutable.ArrayBuffer


/**
 * Created by et on 26/02/15.
 */
class ProcessorHub {
  val processors = new ArrayBuffer[PreProcessor]
  def register(i:PreProcessor):Unit={
    processors += i
  }

  def parse(s:String):String = {
    var ret = s
    for{processor<- processors}{
      ret = processor.parse(ret)
    }
    ret
  }
}
