package misc

import misc.Utils._

import scala.collection.immutable.Stream._
import scala.io.Source
import scala.reflect.io.Path
import scala.util.control.NonFatal
/**
 * Created by et on 1/31/15.
 */


class ParseException extends Exception{
  var lineStr:String = null
  var path:Path = null
}
class Parser {
  /*
  * This parser is to parse the treebank data provided
  *
  *
  * */

  val r = """^\s*\[(.*)\]\s*$""".r
  val r2 = """(.*[^\\])/(.*)""".r
  val r3 = """^\s*$""".r
  var lowercase = true
  def isEmpty(s:String)=r3.findFirstIn(s).nonEmpty
  val sb = new StringBuilder
  def parse(p:Path):Seq[Seq[Token]]= {
    /*
    * Take path, output a stream of a section (sentence),
    * represented by a stream of token.
    * */
      val delim = """=+"""
      val arr = split(Source.fromFile(p.path).getLines().toStream, delim)
    try {
      arr.filter(!isEmpty(_)).map(parseSection)
    }catch{
      case pe:ParseException=>{
        pe.path = p
        throw pe
      }
    }
  }
  def split(s:Seq[String],delim:String):Stream[String]={
    /*
    * Split s by a delimiter, and form a stream of strings.
    * */
    val (l1,l2) = s.span(!_.matches(delim))
    l1.mkString("\n") #:: (if(l2.nonEmpty) split(l2.tail,delim) else Empty)
  }
  private def parseSection(s:String):Section={
    /*
    * section is separated by delimiter "=============================", this function is to
    * parse a section to a stream of token.
    * */
    (STARTSTR,STARTTAG) #:: (Source.fromString(s).getLines().filter(!isEmpty(_))
    .flatMap(parseLine).toStream :+ (STOPSTR,STOPTAG) )
  }
  private def parseLine(s:String):Seq[Token]={
    /*
    * for each line, tokenize them and return a sequence of tokens
    * each token is represented as a tuple (String,Tag), namely,
    * the spelling of the word and the tag of it.
    * */
    try {
      val tr = r.findFirstMatchIn(s)
      val ss = if(tr.nonEmpty) tr.get.group(1) else s
      ss.split(" ").filter(!isEmpty(_))
        .map(s => {
        val mm = r2.findFirstMatchIn(s)
        val m = mm.get

        if(lowercase)
          (m.group(1).toLowerCase, m.group(2))
        else
          (m.group(1), m.group(2))
      })
    }catch{
      case NonFatal(e)=>{
        val pe = new ParseException()
        pe.lineStr = s
        println(s)
        throw pe
      }
    }

  }
}
