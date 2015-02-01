import java.io.{PrintWriter, FileOutputStream, ObjectOutputStream}
import java.nio.file.{FileVisitOption, Files}

import scala.io.Source
import scala.reflect.io.Path
import scala.util.Random

/**
 * Created by et on 1/31/15.
 */
object CorpusManager {
  def combineAll(p:Path,t:Path)={
    val pw = new PrintWriter(t.path)
    val s = p.walk.filter(_.path.toLowerCase.endsWith("pos")).map(p=>Source.fromFile(p.path).mkString).foreach(pw.println(_))
    pw.close()
  }
  val DefaultDelim = "==============================================\n"
  def split(prob:Double,s:Path,l:Path,r:Path,d:String = DefaultDelim)= {
    val p = new Parser()
    assert(prob<1.0 && prob>0.0)
    val rand = new Random(System.currentTimeMillis())
    val ss = p.split(Source.fromFile(s.path).getLines().toStream,"""=+""")
    val lpw = new PrintWriter(l.path)
    val rpw = new PrintWriter(r.path)
    ss.foreach(s=>{
      if(rand.nextDouble()<prob)
        lpw.println(DefaultDelim+s)
      else rpw.println(DefaultDelim+s)
    })
    lpw.close();rpw.close()
  }
}
