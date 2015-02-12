import BIDMat.{FMat, FND}
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import Utils._

/**
 * Created by et on 2/11/15.
 */
class HMMTrigKatz extends HMMTrig {
  override def learnTransition(t: Seq[Seq[(String, Tag)]]) = {
    val V = E.size
    T1 = zeros(V,1)
    T2 = zeros(V,V)
    T3 = FND(V,V,V)
    //    T = sdzeros(sz*sz,sz*sz)
    P = FND(V,V,V)
    for(s<-t){
      val st = s.map(e=>toIdxArr(e._2))
      for{(ps,ms,cs)<-(st,st.tail,st.tail.tail).zipped
          p<-ps
          m<-ms
          c<-cs}{
        T3(p,m,c) += 1
      }
    }
    //bigram count
    T2 = T3.sum(2).toFMat(V,V)//
    T2(toIdx(STOPTAG),toIdx(STOPTAG1)) = T3(?,toIdx(STOPTAG),toIdx(STOPTAG1)).sum(0)(0,0,0)
    //unigram count
    T1 = sum(T2,2) //T1 is a column vector
    //beware of the ENDTAG PROBLEM
    T1(toIdx(STOPTAG1)) = sum(T2(?,toIdx(STOPTAG1)),1)

    //allgrams done, calculate Pb1 first
    val Pb1 = T1 / sum(T1) //Pb1 is a column vector
    //good turing for tri,bi unigram
    val R3 = goodEstimate(T3.data.map(_.toInt))
    val R2 = goodEstimate(T2.data.map(_.toInt))
    //use the estimate, fix the T2 and T3
    val T2T = zeros(V,V)
    val T3T = FND(V,V,V)
    for{i<- 0 until V // TODO: vectorize this ugly loop
        j<- 0 until V}{
        T2T(i,j) = R2(T2(i,j).toInt)
      for{ k<- 0 until V}{
        T3T(i,j,k) = R3(T3(i,j,k).toInt)
      }
    }

    //calculate Pb2 first
    val Pb2 = zeros(V,V)
    //calculate alpha for pb2
    var alpha = zeros(V,1)
    //could i do it in matrix manner? NO, tricky but works
    //build two matricies, T, F
//    var U = 1 - sum((T2 > 0) *@ T2T / T1.t , 2) //this is problematic, 0.0*NaN = NaN, 0.0/0.0 = NaN, kill everything instantly
//    var D = sum((T2 <= 0) *@ T1.t,2)     //the divisor is
    //I give up, just use ugly loop
    for{i<- 0 until V}{
      var U = 0.0
      var D = 0.0
      for{j<- 0 until V}{
        if(T2(i,j)>0) U += T2T(i,j)/T1(j)
        else D += Pb1(j)
      }
      alpha(i) = (1-U)/D
    }

    for{i<- 0 until V
        j<- 0 until V}{
      val r = T2(i,j).toInt
      Pb2(i,j) = if(r>0) R2(r)/T1(i) else alpha(i)*Pb1(j)
      if(Pb2(i,j)==0 || Pb2(i,j).isNaN){
        println("found out zero entry, it should not be though")
      }
    }

    //Pb2 fin, to Pb3, //TODO: vectorize this crazy loop
    alpha = zeros(V,V)
    val Pb3 = FND(V,V,V)
    for{i<- 0 until V
        j<- 0 until V}{
      var U = 0.0
      var D = 0.0
      for(k<- 0 until V){
        if(T3(i,j,k)>0) U += T3T(i,j,k)/T2(i,j)
        else D+= Pb2(j,k)
      }
      alpha(i,j) = ((1-U) / D).toFloat
    }
    for{i<- 0 until V
        j<- 0 until V
        k<- 0 until V}{
      val r = T3(i,j,k).toInt
      Pb3(i,j,k) = if(r>0) R3(r)/T2(i,j) else alpha(i,j)*Pb2(j,k)
    }


    P = Pb3
    //finally, finished, hell!
  }
  private def goodEstimate(i:Seq[Int]):FMat={
    //returning a column vector, with rsharp, originally,
    //now change to estimate d_r
    val t = i.groupBy(e=>e).mapValues(s=>s.length)//another smoothing here
    val rmax = t.keySet.max//n_{r+1} needs to be pretendded as existed
    val arr = Array.tabulate(rmax+2){idx=>{
          t.getOrElse(idx,0)
        }
    }
    val di = arr.zip(arr.tail).map(e=>{
        val nrp1 = e._2
        val nr=e._1
        (nrp1/nr.toFloat)
    })
    (col(0 to rmax) + 1)*@ col(di)//each element plus 1
  }
}
