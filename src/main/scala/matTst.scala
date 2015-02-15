import java.io.{FileInputStream, ObjectInputStream, FileOutputStream, ObjectOutputStream}

import scala.util.Random


/**
 * Created by et on 2/3/15.
 */

import BIDMat.{CMat, CSMat, DMat, Dict, FMat, FND, GMat, GIMat, GSMat, HMat, IDict, Image, IMat, LMat, Mat, SMat, SBMat, SDMat}
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import BIDMat.Solvers._
import BIDMat.Plotting._

object matTst extends App{

  for{i<- 0 until 10
      j<-0 until 10
      k<- 0 until 10}{

    println(i,j,k)
  }

}
