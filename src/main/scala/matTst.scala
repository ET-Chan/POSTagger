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

  val arr = new Array[FMat](10)
  val oos = new ObjectOutputStream(new FileOutputStream("./res/testMul"))
  for(i<-0 until 10){
    oos.writeObject(Random.nextString(100))
  }
  oos.close()
  val ois = new ObjectInputStream(new FileInputStream("./res/testMul"))
  for(i<-0 until 10){
    val obj:String = ois.readObject().asInstanceOf[String]
    println (obj)
  }


}
