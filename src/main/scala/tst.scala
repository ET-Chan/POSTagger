import java.nio.charset.Charset
import java.nio.file.Files

import misc.{CorpusManager, Parser}
import misc.Utils._

import scala.collection.mutable
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

import scala.reflect.io.Path

object tst extends App{
//
//  val corpName = "./res/outcorp"
//  val tagSet = new mutable.HashSet[String]()
//  val p = new Parser()
//  p.parse(corpName).flatten.foreach{case (_,tag)=>{
//    val tagarr = tag.split('|')
//    for {t<- tagarr}
//      tagSet += t
//  }}
//  tagSet -= STARTTAG
//  tagSet -= STOPTAG
//  tagSet += PADDINGTAG
//
//  val tagmap = tagSet.zipWithIndex.toMap
//  val rTagMap = tagmap.map{case (k,v)=>(v.toString,k)}
//
//  implicit val formats = DefaultFormats
//
//  scala.tools.nsc.io.File("./res/tagmap.json").writeAll(compact(render(tagmap)))
//  scala.tools.nsc.io.File("./res/rtagmap").writeAll(compact(render(rTagMap)))
  CorpusManager.convertToLua("./res/outcorp","./res/outcorpulua")

}