package bigram

import BIDMat.FND
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import misc.Utils._

/**
 * Created by et on 19/02/15.
 */
class HMMKN extends HMM{
  override def learnTransition(t: Seq[Seq[(String, Tag)]]): Unit = {
    val V = E.size
    count(t)

    val C1 = T1;val C2 = T2


    //calculate D firstly,
    val n1 = (C2 == 1).data.sum
    val n2 = (C2 == 2).data.sum
    val D = n1 / (n1 + 2 * n2)

    val Pkn1Ndd = (C2 > 0).data.sum
    val Pkn1 = (sum(C2 > 0,1) / Pkn1Ndd).t
    val Pkn2 = zeros(V,V)

    val Nd2 = sum(C2 > 0,2)
    for{i<- 0 until V
        j<- 0 until V}{
      Pkn2(i,j) = Math.max(0,C2(i,j)-D)/C1(i) + D/C1(i)*Nd2(i)*Pkn1(j)
    }

    P = Pkn2
  }
}
