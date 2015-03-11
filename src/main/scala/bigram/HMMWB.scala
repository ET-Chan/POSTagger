package bigram

import misc.Utils._
import BIDMat.SciFunctions._
import BIDMat.MatFunctions._
/**
 * Created by et on 19/02/15.
 */
class HMMWB extends HMM{

  /**
   * This is implementing whitten-bell smoothing
   * based on chen and goodman paper.
   */
    override def learnTransition(t: Seq[Seq[(String, Tag)]]): Unit = {
      val V = E.size
      //    P = FND(V,V,V)
      count(t)
      //for clairty, define some alias
      val C1 = T1;val C2 = T2
      //fist, calculate, N1+, which will be represented as N2 N3, for(bi)trigram N1+
      val N2 = sum(C2 > 0,2)//diminish column dimension
      //for god sake i will change everything to FND soon.
      //lambda, lambda2(3) represent lambda for bi(tri)gram
      //because of the crazy NaN issue, everything will change to forloop now
      val lambda2 = zeros(V,1)
      val c2sum = sum(C2,2)
      for {i <- 0 until V} {
        if (N2(i) == 0)
          lambda2(i) = 1.0
        else {
          lambda2(i) = 1.0 - (N2(i) / (N2(i)+c2sum(i)))
        }
      }
      val Pml1 = C1 / sum(C1)//this is fine
      val Pml2 = C2 / C1//this is fine as well, Pml1 should all be non zero entry
      //for ml , due to lack support for edge operator in FND
      assert(Pml2.data.forall(!_.isNaN))
      val Pwb2 = lambda2 *@ Pml2 + (1-lambda2) *@ Pml1
      P = Pwb2
    }


}
