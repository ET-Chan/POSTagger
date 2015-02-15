import BIDMat.FND
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import Utils._

import scala.util.control.NonFatal

/**
 * Created by et on 15/02/15.
 * This is implementing whitten-bell smoothing
 */
class HmmTrigWB extends HMMTrig {
  override def learnTransition(t: Seq[Seq[(String, Tag)]]): Unit = {
    val V = E.size
//    P = FND(V,V,V)
    count(t)
    //for clairty, define some alias
    val C1 = T1;val C2 = T2;val C3 = T3;
    //fist, calculate, N1+, which will be represented as N2 N3, for(bi)trigram N1+
    val N2 = sum(C2 > 0,2)//diminish column dimension
    val N3 = ((C3 > 0).sum(2)).toFMat(V,V)//dimnish third dimension
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

//    val lambda2 = 1 - ( N2/(N2 + sum(C2,2))  )
    val lambda3 = zeros(V,V)
    //val lambda3 =  (( (N3/(N3 + C3.sum(2) )) *@ -1 ) + 1).toFMat(V,V)
    val c3sum = C3.sum(2).toFMat(V,V)
    for{i<-0 until V
        j<-0 until V}{
      if(N3(i,j)== 0)
        lambda3(i,j) = 1.0f
      else
        lambda3(i,j) = 1.0f - (N3(i,j)/(N3(i,j)+c3sum(i,j)))

    }
    val Pml1 = C1 / sum(C1)//this is fine
    val Pml2 = C2 / C1//this is fine as well, Pml1 should all be non zero entry
    //for ml , due to lack support for edge operator in FND
    assert(Pml2.data.forall(!_.isNaN))
    val Pml3 = C3 // this is problematic, 0/0 is NaN
    for{i<- 0 until V
        j<- 0 until V}{
      //ugly hack
      val divisor = if(T2(i,j) == 0) 1 else T2(i,j)
      Pml3(i,j,?) /= divisor//wait, something wrong?
    }
    assert(Pml3.data.forall(!_.isNaN))
    val Pwb2 = lambda2 *@ Pml2 + (1-lambda2) *@ Pml1

    val Pwb3 = FND(V,V,V)
    for{i<- 0 until V
        j<- 0 until V
        k<- 0 until V}{
      try {
        Pwb3(i, j, k) = lambda3(i, j) * Pml3(i, j, k) + (1 - lambda3(i, j)) * Pwb2(j, k)
      }catch{
        case NonFatal(e)=>{
          println(e)
          println(i,j,k)
          throw e
        }
      }
    }
    P = Pwb3
    cleanTransTable()
  }
}
