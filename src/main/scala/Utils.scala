/**
 * Created by et on 1/31/15.
 */
object Utils {
  type Tag = String
  type Token = (String,Tag)
  type Section = Seq[Token]
  class MutableInt(var v:Int=0){
    def +=(rhs:Int) ={
      v+=rhs
    }
    def set(rhs:Int)={v=rhs}
    def get=v
  }
}
