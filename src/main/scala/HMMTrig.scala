import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import BIDMat._
import Utils._

import scala.Predef._
import scala.collection.immutable.Stream._
import scala.collection.mutable.HashMap

class HMMTrig extends Serializable{
//  Mat.checkMKL
  val printIters = 1
  var lock = false
  var T1:FMat=null
  var T2:FMat=null
  var T3:FND=null
//  var T:DMat = null
  var P:FND=null
  var E:Array[HashMap[String,Double]] = null
  var M:Map[String,Int] = null//table for tagToIdx
  var BM:Array[String] = null//table for idxToTag
  def learn(wd:Seq[Token],tr:Seq[Seq[Token]]):Unit={
    if(lock) throw new Exception("The model is learned!")
    def padding(e:Token):Seq[Token]={
      if(e._2==STARTTAG)
        Seq((STARTSTR1,STARTTAG1),(STARTSTR,STARTTAG))
      else if(e._2==STOPTAG)
        Seq((STOPSTR,STOPTAG),(STOPSTR1,STOPTAG1))
      else
        Seq(e)
    }
    val wdm = wd.flatMap(padding)
    val trm = tr.map(_.flatMap(padding))

    learnEmission(wdm)
    learnTransition(trm)
    lock = true
  }
  def cleanTransTable()={
    T1 = null;T2=null;T3=null;
  }
  def learnTransition(t:Seq[Seq[Token]]):Unit={
    val sz = E.size
    val delta:Float = 1
    T1 = zeros(sz,1)
    T2 = zeros(sz,sz)
    T3 = FND(sz,sz,sz)
//    T = sdzeros(sz*sz,sz*sz)
    P = FND(sz,sz,sz)
    for(s<-t){
      val st = s.map(e=>toIdxArr(e._2))
//      //for unigram,_2 is the tag
//      st.flatten.foreach(e=>{T1(e)+=1})
      //for bigram
//      for{(ps,cs)<- st.zip(st.tail)
//        p<-ps
//        c<-cs
//      }{
//        T2(p,c)+=1
//      }
      //for trigram
      for{(ps,ms,cs)<-(st,st.tail,st.tail.tail).zipped
        p<-ps
        m<-ms
        c<-cs}{
        T3(p,m,c) += 1
      }
    }
    //bigram count
    T2 = T3.sum(2).toFMat(sz,sz)
    //beware of the END and ENDTAG PROBLEM,
    T2(toIdx(STOPTAG),toIdx(STOPTAG1)) = T3(?,toIdx(STOPTAG),toIdx(STOPTAG1)).sum(0)(0,0,0)
    //unigram count
    T1 = sum(T2,2)
    //beware of the ENDTAG PROBLEM
    T1(toIdx(STOPTAG1)) = sum(T2(?,toIdx(STOPTAG1)),1)
    //learn adaptively
    P = T3 + delta
    for{i<- 0 until sz
        j<- 0 until sz}{
      P(i,j,?) /= (delta*sz+T2(i,j))
    }

    //initiate T table for viberti algorithm
//    initTV()
    cleanTransTable()
  }
//  private def initTV()={
//    val V = E.size
//    T = dzeros(V*V,V*V)
//    for{i<- 0 until V}{
//      for{j<-0 until V}{
//        T(bi2Uni(i,j), bi2Uni(j,0) until bi2Uni(j,V)) = P(i,j,?).toFMat(1,V)
//      }
//    }
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
//  private def predictSeg(s:Stream[String]):Stream[Tag]={
//    //First of all, the string must be prepend and append with STARTSTR and STOPSTR respectively.
//    assert(s.head == STARTSTR && s.last == STOPSTR)
//    //PREPAND STARTSTR1 and APPEND STOPSTR1, reduce trivial cases
//    val sp = ((STARTSTR1 #:: s) :+ STOPSTR1).toArray
//    //then, viberti algorithm, completely the same with bigram case
//    //except we now have each state represent a bigram
//    //init dp table
//    val V = E.size
//    val dp = dzeros(V*V,V*V)
//    val sz = sp.length
//    val R = izeros(sz,V*V)
//    R(0,?) = -1 //it is nonsense to backtrack from 0 and 1
//    R(1,?) = -1
//
//    //STARTTAG1 and STARTTAG bounds to be the start TAG of the
//    // sequence, other could just go to sleep
//    dp(toIdx(STARTTAG1,STARTTAG),?) = 1.0
//    for(i<-2 until sz){//No need to start from 0
//      val ep = getEmissionProbTri(sp(i))//is this efficient? I dont really care for now.
//      val Tr = dp *@ T
//      val (maxv,maxi) = maxi2(Tr,1)
//      R(i,?) = maxi
//      dp ~ (dp*@0) + (maxv.t *@ ep)
//    }
//    val res = new Array[Int](sz)
//    res(sz - 1) = maxi2(dp(?,0))._2(0,0)
//    res(0) = toIdx(STARTTAG1)
//    for(i<-sz-2 to 1 by -1){//0th and 1st is unnecessary, it must be
//      res(i) = R(i+1,res(i+1))
//    }
//    //TODO
//    res.map(e=>{
//      toTag(uni2Bi(e)._2)
//    }).toStream
//  }


  //viberti iterative
  //matrix is just too freaking slow, because of non-fully supported sparse matrix in scala
  //we will just rewrite an iterative version of it
  private def predictSeg(s:Stream[String]):Stream[Tag]= {
    val V = E.size
    //First of all, the string must be prepend and append with STARTSTR and STOPSTR respectively.
    assert(s.head == STARTSTR && s.last == STOPSTR)
    //PREPAND STARTSTR1 and APPEND STOPSTR1, reduce trivial cases
    val sp = ((STARTSTR1 #:: s) :+ STOPSTR1).toArray
    val sz = sp.length
    val R = Array.tabulate(sz){_=>izeros(V,V)}

    R(0)(?) = -1
    R(1)(?) = -1
    val prev = dzeros(V,V)
    val PP = Array.tabulate(V){i=>{
        DMat(P(?,?,i).toFMat(V,V))
      }
    }
    prev(toIdx(STARTTAG1),toIdx(STARTTAG))=1.0//everything is impossible
    val curr = dzeros(V,V)
    for(i<-2 until sz){//No need to start from 0
      val ep = getEmissionProb(sp(i))
      for(k<-0 until V){
        val (maxv,maxi) = maxi2(prev *@ PP(k))
        curr(?,k) = maxv^*ep(k)
        R(i)(k,?) = maxi
      }

      prev <-- curr
    }
    //traceback
    def max(mat:DMat):(Int,Int)={
      val (m,n) = size(mat)
      var (x,y) = (-1,-1)
      var maxv = Double.MinValue
      for{i<-0 until m
          j<-0 until n}{
        if(mat(i,j)>maxv){
          maxv = mat(i,j)
          x = i;y=j
        }
      }
      (x,y)
    }
    val res = new Array[(Int,Int)](sz)
    res(sz - 1) = max(curr)
    for(i<-sz-2 to 1 by -1){//0th and 1st is unnecessary, it must be
      val (j,k) = res(i+1)
      res(i) = (R(i+1)(k,j),j)
    }
    res(0) = (-1,toIdx(STARTTAG1))
    res.map(e=>toTag(e._2)).toStream

    //    res(sz - 1) = maxi2(dp(?,0))._2(0,0)
    //    res(0) = toIdx(STARTTAG1)
//    Array("").toStream

  }


  //we need a pair of method, to transform (i,j) into a monster int
  private def bi2Uni(i:Int,j:Int):Int=i*E.size+j
  private def uni2Bi(i:Int):(Int,Int)=(i/E.size,i%E.size)

  private def getEmissionProbTri(s:String):DMat={
    val V = E.size
    val epb = getEmissionProb(s)
    //epb is a column vector save the tag emission probability with respect to s
    val ept = dzeros(V,V)
    //ept is a matrix, each column is exactly the same with epb
    ept ~ (ept *@ 0 ) + epb
    //ept(?) will reshape ept into V*V vector, in column-major order, which makes ept(?)
    // ept(?)(toIdx(tag_i,tag_j)) is the ep of tag_j with respect to s
    ept(?)
  }

  private def getEmissionProb(s:String):DMat= {
    //construct a conlumn, grabbing the emission probability of this string for every tag.
    val r = dcol(E.zipWithIndex.map(h => h._1.getOrElse(s, 0.0)).toList)

    if(sum(r(?))(0,0)==0)//element-wise summation
      r(?)=1.0d//all element become one, do not terminate decoding
    r
  }
  def validate(su:Seq[Seq[Token]]):Double={
    var i = 0
    val (s,c) = su.map(_.toArray).map(arr=>{
      //each entry of s stores count of each section,
      // c stores count of each correct prediction
      val p = predictSeg(arr.map(_._1).toStream)
      //remove the first and last element
      val pp = p.tail.dropRight(1)
      val c = arr.map(_._2)
      if(i% printIters == 0)
        println(s"Validating ${i}th section")
      i += 1
      (p.size,pp.zip(c).count(e => e._1 == e._2))
    }).unzip
    c.sum.toDouble/s.sum
  }


  def printEmission()=println(E)
  def printTransition()= {
    println(T1)
    println(T2)
    println(T3)
  }
  def toIdx(s:Tag):Int=M(s)
  def toIdx(s1:Tag,s2:Tag):Int=bi2Uni(toIdx(s1),toIdx(s2))
  def toIdxArr(tag:Tag):Seq[Int]=tag.split("""\|""").map(toIdx)
  def toTag(i:Int):Tag=BM(i)
  def save()={
    val sz = E.size
    saveFMat("res/transition.lz",P.toFMat(1,sz*sz*sz))
    val oos = new ObjectOutputStream(new FileOutputStream("res/misc.dat"))
    oos.writeObject(E)
    oos.writeObject(M)
    oos.writeObject(BM)
    oos.close()
  }
  def load()={
    val ois = new ObjectInputStream(new FileInputStream("res/misc.dat"))
    E = ois.readObject.asInstanceOf[Array[HashMap[String,Double]]]
    val sz = E.size
    M = ois.readObject.asInstanceOf[Map[String,Int]]
    BM = ois.readObject.asInstanceOf[Array[String]]
    P = FND(loadFMat("res/transition.lz")).reshape(sz,sz,sz)
    lock = true
  }
}