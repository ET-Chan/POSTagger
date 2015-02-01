import scala.collection._
import scala.reflect.io.Path
import Utils._
import breeze.linalg._
import scala.collection.mutable.HashMap

/**
 * Created by et on 1/31/15.
 */



class HMM {
  var lock = false
  var T=DenseMatrix.eye[Double](0)
  val E=new HashMap[String,HashMap[String,Double]]
  var M:Map[String,Int] = null
  var BM:Array[String] = null
  def learn(wd:Seq[Token],bi:Seq[Token]):Unit={
    if(lock) throw new Exception("The model is learned!")
    learnEmission(wd)
    buildIdx
    learnTransition(bi)
    lock = true
  }
  private def buildIdx= {
    val L = E.keySet.zipWithIndex
    BM = new Array[String](L.size)
    L.foreach(e=>BM(e._2)=e._1)
    M=L.toMap
  }
  def learnTransition(t:Seq[Token]):Unit={
    T = DenseMatrix.zeros[Double](E.size,E.size)


  }
  def learnEmission(t:Seq[Token]):Unit={
    println(t)
    val m = t.flatMap(e => {
              val (s, tag) = e
              val t = Seq(s, s).zip(tag.split("""\|"""))
              t
            })
      .groupBy(_._2)
      .mapValues(_.groupBy(_._1).mapValues(_.length))
    //probability estimation

    for {(tag, tl) <- m}{
      E.put(tag,new HashMap[String,Double])
      val sum = tl.map(_._2).sum
      for{(str,num)<-tl}
        E(tag).put(str,num.toDouble/sum)

    }
  }
  def predict(s:Seq[String]):Seq[Tag]={Array("")}
//  def write(p:Path):Unit={}
//  def read(p:Path):Unit={}
  def printEmission={
    println(E)
  }
  def toIdx(s:Tag):Int=M(s)
  def toTag(i:Int):Tag=BM(i)
}
