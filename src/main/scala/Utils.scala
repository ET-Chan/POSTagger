import java.nio.file.Path

/**
 * Created by et on 1/31/15.
 */
object Utils {
  type Tag = String
  type Token = (String,Tag)
  type Section = Seq[Token]
  type Excptn  = (String,Path,Int)
  class MutableInt(var v:Int=0){
    def +=(rhs:Int) ={
      v+=rhs
    }
    def set(rhs:Int)={v=rhs}
    def get=v
  }
  //STOPSTR and STARTSTR must be lower case.
  val STOPSTR = "**end***";val STOPTAG = "END";
  val STOPSTR1 = "***end1***";val STOPTAG1 = "END1";

  val STARTSTR = "***start0***";val STARTTAG="START0";
  val STARTSTR1 = "***start1***";val STARTTAG1="START1";


}
