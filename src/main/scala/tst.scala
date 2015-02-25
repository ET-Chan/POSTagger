import java.io.{FileInputStream, ObjectInputStream, FileOutputStream, ObjectOutputStream}

import scala.util.Random
import misc.NumberProcessor


/**
 * Created by et on 2/3/15.
 */

import BIDMat.{CMat, CSMat, DMat, Dict, FMat, FND, GMat, GIMat, GSMat, HMat, IDict, Image, IMat, LMat, Mat, SMat, SBMat, SDMat}
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import BIDMat.Solvers._
import BIDMat.Plotting._

object tst extends App{

  val np = new NumberProcessor
  println(np.parse("114.332"))
  println(np.parse("4/23"))
  println(np.parse("124363462"))

}
