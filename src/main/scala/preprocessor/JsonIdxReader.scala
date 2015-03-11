/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package preprocessor

import misc.Utils._
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

import scala.io.Source

/**
 * Created by et on 26/02/15.
 */
object JsonIdxReader {
  /**
   * This is for interfacing with json-based format
   * Getting the word-indicies mapping and reversed mapping from the json file
   * */
  implicit val formats = DefaultFormats

  def getIdx(p:String = "res/wordIdx.json"):Map[String,Int]={
    val text = Source.fromFile(p).mkString
    parse(text).extract[Map[String,Int]]
  }
  def getRIdx(p:String = "res/rWordIdx.json"):Map[Int,String]={
    val text = Source.fromFile(p).mkString
    val ret = Map(RAREIDX->RARESTR,RARENUMBERIDX->RARENUMBERSTR)
    ret ++ parse(text).extract[Map[String,String]].map{case (k,v)=> (k.toInt,v)}
  }
}
