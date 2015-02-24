package trigram

import BIDMat.FND
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import misc.Utils
import Utils._

/**
 * Created by et on 15/02/15.
 * This is implementing Knesey-Ney smoothing
 */
class HMMTrigKN extends HMMTrig{
  override def learnTransition(t: Seq[Seq[(String, Tag)]]): Unit = {
    val V = E.size
    count(t)

    val C1 = T1;val C2 = T2;val C3 = T3


    //calculate D firstly,
    var n1 = (C2 == 1).data.sum
    var n2 = (C2 == 2).data.sum
    var D = n1 / (n1+2*n2)

    val Pkn1Ndd = (C2 > 0).data.sum
    val Pkn1 = (sum(C2 > 0,1) / Pkn1Ndd).t
    val Pkn2 = zeros(V,V)
//    val dN  = (C3 > 0).sum(0).toFMat(V,V)
//    //this one is tricky, we have to diminish two dimensions
//    val dNd = (C3 > 0).sum(0,2).toFMat(V,1)
//    for{i<- 0 until V
//        j<- 0 until V}{
//      if(dN(i,j) == 0)
//        Pkn2(i,j) = 0.0f
//      else
//        Pkn2(i,j) = dN(i,j)/dNd(i)
//    }
    val Nd2 = sum(C2 > 0,2)
    for{i<- 0 until V
        j<- 0 until V}{
      Pkn2(i,j) = Math.max(0,C2(i,j)-D)/C1(i) + D/C1(i)*Nd2(i)*Pkn1(j)
    }
    n1 = (C3 == 1).data.sum
    n2 = (C3 == 2).data.sum

    D = n1 / (n1+2*n2)
    val Nd  = (C3 > 0).sum(2).toFMat(V,V)
    //Calculate Pkn3
    val Pkn3 = FND(V,V,V)
    for{i<- 0 until V
        j<- 0 until V
        k<- 0 until V}{
      if(C2(i,j) != 0)
        Pkn3(i,j,k) = Math.max(0, C3(i, j, k) - D) / C2(i, j) +
          D / C2(i, j) * Nd(i, j) * Pkn2(j, k)
    }
    P = Pkn3
  }
}
