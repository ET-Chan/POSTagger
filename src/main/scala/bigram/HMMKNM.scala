package bigram

import BIDMat.FND
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import misc.Utils._

/**
 * Created by et on 19/02/15.
 */
class HMMKNM extends HMM{
  /*
  * This is Knesey-Ney modified smoothing
  * implemeted base on chen and goodman paper
  * */
  override def learnTransition(t: Seq[Seq[(String, Tag)]]): Unit = {
    val V = E.size
    count(t)

    val C1 = T1;val C2 = T2


    val Pkn1Ndd = (C2 > 0).data.sum
    val Pkn1 = (sum(C2 > 0,1) / Pkn1Ndd).t
    //calculate D firstly,
    val n1 = (C2 == 1).data.sum
    val n2 = (C2 == 2).data.sum
    val n3 = (C2 == 3).data.sum
    val n4 = (C2 == 4).data.sum
    val Y = n1 / (n1 + 2 * n2)
    val D1 = 1.0 - 2 * Y * n2 / n1
    val D2 = 2.0 - 3 * Y * n3 / n2
    val D3p = 3.0 - 4 * Y * n4 / n3
    var gamma = (D1 * sum( C2==1 ,2)
      + D2 * sum( C2==2 ,2)
      + D3p * sum( C2>=3 ,2)) / C1

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
    def D(in:Int):Double = in match{
      case 0=>0.0
      case 1 =>D1
      case 2 =>D2
      case _ =>{assert(in>0);D3p}
    }
    val Nd2 = sum(C2 > 0,2)
    for{i<- 0 until V
        j<- 0 until V}{
      Pkn2(i,j) = (Math.max(0,C2(i,j)-D(C2(i,j).toInt))/C1(i) + gamma(i)*Pkn1(j)).toFloat
    }

    P = Pkn2
  }
}
