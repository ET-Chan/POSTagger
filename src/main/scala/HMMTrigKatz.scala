import BIDMat.{FMat, FND}
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import Utils._

/**
 * Created by et on 2/11/15.
 * This is implementing Katz smoothing
 */
class HMMTrigKatz(K:Int = 0,K2:Int=0,beta:Double=1.0) extends HMMTrig {
  override def learnTransition(t: Seq[Seq[(String, Tag)]]) = {
    val V = E.size
    count(t)



    //allgrams done, calculate Pb1 first
    val Pb1 = T1 / sum(T1) //Pb1 is a column vector
    //good turing for tri,bi unigram
    val R3 = goodEstimate(T3.data.map(_.toInt),K)
    val R2 = goodEstimate(T2.data.map(_.toInt),K2)


    //calculate Pb2 first
    val Pb2 = zeros(V,V)
    //calculate alpha for pb2
    var alpha = zeros(V,1)
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
    //Pb2 fin, to Pb3, //TODO: vectorize this crazy loop
    alpha = zeros(V,V)
    val Pb3 = FND(V,V,V)
    for{i<- 0 until V
        j<- 0 until V
        k<- 0 until V}{
      val r = T3(i,j,k).toInt
      if(r>0){
        val dr = if (r>K) 1 else R3(r)
        Pb3(i,j,k) = dr * r / T2(i,j)
      }
   }
    //deal with zero entry
    //I will give up matrix form and rewrite everythin to loop form, at least at this stage
    //code become ridiculously complex and redundant.
    alpha = zeros(V,V)
    for{i<- 0 until V
        j<- 0 until V}{
      var U = 0.0
      var D = 0.0
      for{k<-0 until V}{
        if(T3(i,j,k)>0) {
          U+= Pb3(i,j,k)
          D += Pb2(j,k)
        }
      }
      alpha(i,j) = ((1-U)/(1-D)).toFloat
    }
    for{i<- 0 until V
        j<- 0 until V
        k<- 0 until V}{
      val r = T3(i,j,k).toInt
      val rhs = alpha(i,j)*Pb2(j,k)
      if(r==0){
//        val debugAlpha = alpha(i,j)
//        val debugPb2 = Pb2(j,k)
        Pb3(i,j,k) = rhs
      }
    }

    P = Pb3
    //finally, finished, hell!
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
