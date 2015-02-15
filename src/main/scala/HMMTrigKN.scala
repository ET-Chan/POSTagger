import BIDMat.FND
import Utils._
import BIDMat.SciFunctions._
import BIDMat.MatFunctions._

/**
 * Created by et on 15/02/15.
 * This is implementing Knesey-Ney smoothing
 */
class HMMTrigKN(D:Double = 1) extends HMMTrig{
  override def learnTransition(t: Seq[Seq[(String, Tag)]]): Unit = {
    val V = E.size
    count(t)
    val C1 = T1;val C2 = T2;val C3 = T3
    //for loop, I am tired of NaN
    val Pkn2 = zeros(V,V)
    val Nd  = (C3 > 0).sum(2).toFMat(V,V)
    val dN  = (C3 > 0).sum(0).toFMat(V,V)
    //this one is tricky, we have to diminish two dimensions
    val dNd = (C3 > 0).sum(0,2).toFMat(V,1)
    for{i<- 0 until V
        j<- 0 until V}{
      if(dN(i,j) == 0)
        Pkn2(i,j) = 0.0f
      else
        Pkn2(i,j) = dN(i,j)/dNd(i)
    }
    //Calculate Pkn3
    val Pkn3 = FND(V,V,V)
    for{i<- 0 until V
        j<- 0 until V
        k<- 0 until V}{
      if(C2(i,j) != 0)
        Pkn3(i,j,k) = (Math.max(0, C3(i, j, k)) / C2(i, j) +
                      D / C2(i, j) * Nd(i, j) * Pkn2(j, k)).toFloat
    }
    P = Pkn3
  }
}
