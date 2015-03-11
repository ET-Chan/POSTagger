package bigram

import BIDMat.FMat
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import misc.Utils
import Utils._

/**
* Created by et on 19/02/15.
*/
class HMMKatz(K2:Int=4) extends HMM{
  /*
  * This is Katz smoothing
  * implemeted base on chen and goodman paper
  * */
  override def learnTransition(t: Seq[Seq[Token]]): Unit = {
    val V = E.size
    count(t)
    val Pb1 = T1 / sum(T1) //Pb1 is a column vector
    val R2 = goodEstimate(T2.data.map(_.toInt),K2)
    val Pb2 = zeros(V,V)
    //calculate alpha for pb2
    val alpha = zeros(V, 1)
    //firstly deal with >0 entry
    for{i<- 0 until V}{
      //      var sum = 0.0
      for{j<- 0 until V}{
        val r = T2(i,j).toInt
        if(r>0) {
          val dr =
            if (r>K2) 1
            else
              R2(r)
          //          sum += dr*r
          Pb2(i, j) = dr*r / T1(i)
        }
      }
      //      if(sum > T1(i))
      //         println("error, it is impossible for bigger than that.")
    }
    //deal with ==0 entry, calculate alpha first

    for{i<- 0 until V}{
      var U = 0.0
      var D = 0.0
      for{j<-0 until V}{
        if(T2(i,j)>0) {
          U+= Pb2(i,j)
          D += Pb1(j)
        }
      }
      alpha(i) = ((1-U)/(1-D)).toFloat

    }

    for{i<- 0 until V
        j<- 0 until V}{
      val r = T2(i,j).toInt
      //      val debugAlpha = alpha(i)
      //      val debugPb1  = Pb1(j)
      val rhs = alpha(i)*Pb1(j)
      if(r==0){
        Pb2(i,j) = rhs
      }
    }
    P = Pb2
  }

  private def goodEstimate(i:Seq[Int],k:Int):FMat={
    //returning a column vector, with rsharp, originally,
    //now change to estimate d_r
    val n = i.groupBy(e=>e).mapValues(s=>s.length)//another smoothing here
    //sanity check
    //    assert(n(46) == i.count(_==46))
    val rmax = n.keySet.max//n_{r+1} needs to be pretendded as existed
    val arr = Array.tabulate(rmax+2){idx=>{
        n.getOrElse(idx,0)
      }
      }
    val di = arr.zip(arr.tail).map(e=>{
      val nrp1 = e._2
      val nr=e._1
      nrp1 / nr.toFloat
    })
    val rs = (col(0 to rmax) + 1 )*@ col(di)//each element plus 1
    val fixTerm = n.getOrElse(k+1,0).toFloat/n(1)*(k+1)//this fixTerm need to be float, cost 1 hour to find this bug.
    //calculate dr now
    val ret = (rs/col(0 to rmax) - fixTerm )/(1-fixTerm) //each element divided by their index r and minus fixTerm, divided by 1-fixTerm
    ret
  }
}
