import java.util.Scanner

import scala.io.Source
import scala.reflect.io.Path
import Utils._
import scala.Stream._
import scala.util.matching.Regex.Match
import scala.util.matching._
import scala.collection.immutable.Stream._
/**
 * Created by et on 1/31/15.
 */
class Parser {
  val sb = new StringBuilder
  def parse(p:Path):Seq[Seq[Token]]= {
    val delim = """=+"""
    val arr = split(Source.fromFile(p.path).getLines().toStream,delim)
    arr.filter(!_.matches("""^\s*$""")).map(parseSection)
  }
  def split(s:Seq[String],delim:String):Stream[String]={
    val (l1,l2) = s.span(!_.matches(delim))
    l1.mkString("\n") #:: (if(l2.nonEmpty) split(l2.tail,delim) else Empty)
  }
  private def parseSection(s:String):Section={
    ("***START***","START") #:: (Source.fromString(s).getLines().filter(!_.matches("""^\s*$"""))
    .flatMap(parseLine).toStream :+ ("***END***","END") )
  }
  private def parseLine(s:String):Seq[Token]={
    val r = """^\s*\[(.*)\]\s*$"""
    val r2= """(.*[^\\])/(.*)"""
    val ss = if(s.matches(r)) r.r.findFirstMatchIn(s).get.group(1) else s
    ss.split(" ").filter(!_.matches("""^\s*$"""))
      .map(s=>{
        val m = r2.r.findFirstMatchIn(s).get
        (m.group(1).toLowerCase,m.group(2))
      }
    )
  }
}
