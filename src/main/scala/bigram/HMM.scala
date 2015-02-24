package bigram

import misc.Utils
import Utils._

import scala.collection.immutable.Stream._
//import breeze.linalg._
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import BIDMat.{DMat, FMat, Mat}

import scala.Predef._
import scala.collection.mutable.HashMap

/**
 * Created by et on 1/31/15.
 */



class HMM extends Serializable{
  Mat.checkMKL
  var lock = false
  var T1:FMat=null
  var T2:FMat=null
  var P:DMat = null
  var E:Array[HashMap[String,Double]] = null
  var M:Map[String,Int] = null//table for tagToIdx
  var BM:Array[String] = null//table for idxToTag
  def learn(wd:Seq[Token],bi:Seq[Seq[Token]]):Unit={
    if(lock) throw new Exception("The model is learned!")
    learnEmission(wd)
    learnTransition(bi)
    lock = true
  }

  def count(t:Seq[Seq[Token]]):Unit={
    val sz = E.size
    T1 = zeros(sz,1)
    T2 = zeros(sz,sz)
    for(s<-t){
      val st = s.map(e=>toIdxArr(e._2))
      for{(ps,ms)<-(st,st.tail).zipped
          p<-ps
          m<-ms}{
        T2(p,m) += 1
      }
    }
    //unigram count
    T1 = sum(T2,2)
    //beware of the ENDTAG PROBLEM
    T1(toIdx(STOPTAG)) = sum(T2(?,toIdx(STOPTAG)),1)
    //sanity-check
    assert(T1.nc == 1)
    assert(T2.nr == sz)
    assert(T2.nc == sz)
    assert(T2.nr == sz)
  }


  def learnTransition(t:Seq[Seq[Token]]):Unit={
    P = dzeros(E.size,E.size)
    count(t)
    T2 += 1.0
    T1 += E.size
    P = T2/T1
    P(toIdx(STOPTAG),?) = 0.0//fix the end NaN problem, otherwise it will pollute


  }

//  def learnTransition(t:Seq[Token]):Unit={
//    T = dzeros(E.size,E.size)
//    val startID = toIdx(STARTTAG);val endID = toIdx(STOPTAG)
//    val ts = t.map(_._2).map(toIdxArr)//map to int tag
//    for{ (ps,cs)<- ts.zip(ts.tail)//words may have multiple tags, iterate all of them
//          p<-ps
//          c<-cs
//    }{
//      if(!((p==startID && c==endID) | (p==endID && c==startID))) {
//        //Not counting START,END. it is ugly fix, but works.
//        T(p, c) += 1
//      }
//    }
//    val Ts = sum(T,2)//sum along with column, return a row vector
//    T = T/Ts
//    T(toIdx(STOPTAG),?) = 0.0//fix the end NaN problem, otherwise it will pollute
//  }
  def learnEmission(t:Seq[Token]):Unit={
    val m = t.flatMap(e => {
              val (s, tag) = e
              val t = Seq(s, s).zip(tag.split("""\|""").toSeq)//it is possible that there exists multiple tag.
              t
            })
      .groupBy(_._2)
      .mapValues(_.groupBy(_._1).mapValues(_.length))//these two groupby forms a table firstly index by tag then string.

    E = new Array(m.size)
    //build index table
    val L = m.keySet.zipWithIndex
    BM = new Array[String](L.size)
    L.foreach(e=>BM(e._2)=e._1)
    M=L.toMap

    //probability estimation
    for {(tag, tl) <- m}{
      val iTag = M(tag)
      E(iTag)=new HashMap[String,Double]
      val sum = tl.map(_._2).sum
      for{(str,num)<-tl}
        E(iTag).put(str,num.toDouble/sum)
    }
  }
  def predict(su:Stream[String]):Stream[Tag]={
    //Doing prediction, automatically cut off from START to END
    //sanity-check
    val sl = su.map(_.toLowerCase)
    if(sl.isEmpty) return Stream.Empty
    assert(sl.head == STARTSTR)

    val (hl,tl) = sl.tail.span(!_.contentEquals(STARTSTR))//split sl into two streams, first streams stop at next STOPSTR,
                                                          // second one starts at next STARTSTR
    //predict a short segment
    predictSeg(STARTSTR #:: hl.toStream).append(predict(tl))
  }


//  private def splitSeqToken(in:Seq[Token]):Seq[Seq[Token]]={
//    if(in.isEmpty) return Stream.Empty
//    assert(in.head == STARTSTR)
//    val (hl,tl) = in.tail.span(e=> !e._2.content)
//
//  }

  private def getEmissionProb(s:String):DMat= {
    //construct a conlumn, grabbing the emission probability of this string for every tag.
    val r = dcol(E.zipWithIndex.map(h => h._1.getOrElse(s, 0.0)).toList)

    if(sum(r(?))(0,0)==0)//element-wise summation
      r(?)=1.0d//all element become one, do not terminate decoding
    r
  }

  def validate(su:Seq[Seq[Token]]):Double={
    val (s,c) = su.map(_.toArray).map(arr=>{
    //each entry of s stores count of each section,
    // c stores count of each correct prediction
      val p = predictSeg(arr.map(_._1).toStream)
      val c = arr.map(_._2)
      (p.size,p.zip(c).count(e => e._1 == e._2))
    }).unzip

    c.sum.toDouble/s.sum
  }

  private def predictSeg(s:Stream[String]):Stream[Tag]={
    val T = P
    val arr = s.toArray
    val sz = arr.size
    val res = new Array[Int](sz)
    //sanity-check
    assert(arr.head == STARTSTR && arr.last == STOPSTR)
    //the first entry should be directly the emit probability of STARTSTR
    val Pc = dones(E.size,E.size)//P is the main matrix, calculating the probability
                                // for each combination of tags at each iteration
    Pc ~ (Pc *@ 0) + getEmissionProb(STARTSTR) //which is tedious, but have to do this to assign the vector to each column of P

    val R = izeros(sz,E.size)//R is for tracing back
    R(0,?) = -1 //undefined
    for(i<- 1 until sz){//Terminate at zero, no need to handle i=0
      val eP = getEmissionProb(arr(i))
      val Tr = T *@ Pc//T element wise multiply P
      val (maxv,maxi) = maxi2(Tr,1)
      R(i,?) = maxi //maxi is the index vector for largest predecessors, making it in R table
      Pc ~ (Pc *@ 0) + (maxv.t *@ eP)//this is P = maxv.t *@ eP, due to the restriction on BIDMat
    }
    //viberti algorithm in Matrix form
    res(sz-1) = maxi2(Pc(?,0))._2(0,0)
    for(i<- sz-2 to 0 by -1){//tracing back.
      res(i) = R(i+1,res(i+1))
    }
    res.map(toTag).toStream
  }
//  def write(p:Path):Unit={}
//  def read(p:Path):Unit={}
  def printEmission()=println(E)

  def printTransition()=println(P)
  def toIdx(s:Tag):Int=M(s)
  def toIdxArr(tag:Tag):Seq[Int]=tag.split("""\|""").map(toIdx)

  def toTag(i:Int):Tag=BM(i)
}
