/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package lua

import java.io.{PrintWriter, OutputStreamWriter, OutputStream, InputStream}


import scala.io.Source
import scala.sys.process.ProcessIO
import scala.sys.process.Process

/**
 * Created by et on 01/03/15.
 */
class Ranker  extends App{
  /**
   * This is an obsolete object
   * Will be deleted in next version.
   * */
  val pio = new ProcessIO(processInput,processOutput,_=>{})

  private def processInput(os:OutputStream):Unit={

  }
  private def processOutput(is:InputStream):Unit={

  }

  def getRank(i:Int):(Array[Int],Array[Float])={
    null
  }

}
