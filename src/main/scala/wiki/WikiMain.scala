package wiki

import java.io._

import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.process.DocumentPreprocessor
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import preprocessor.{JsonIdxReader, LowerCaseProcessor, NumberProcessor}

import scala.collection.JavaConversions._
import scala.collection.concurrent.TrieMap
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source
import scala.reflect.io.Path
import misc.Utils._

/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */
object WikiMain extends App {
  val thread = 4

//  val ctmap = new TrieMap[String,Int]()


//
//  val statsPath = "./res/wikistats.dat"
//
//  val ois = new ObjectInputStream(new FileInputStream(statsPath))
//  val ctlist = ois
//    .readObject()
//    .asInstanceOf[TrieMap[String,Int]]
//    .toList
//    .sortBy(_._2)
//    .reverse
//    .toArray
//  val comIdx = 200000
//
//  val sum = ctlist.map(_._2).sum
//  val rarecounter = ctlist.take(comIdx).map(_._2).sum
//
//  println(s"Total: $sum, Rare: $rarecounter, Ratio:${sum.toDouble/rarecounter}")


//  val path = "/home/et/IdeaProjects/POSTagger/res/wiki/extractedTagOut"
//  val tPath = "/home/et/IdeaProjects/POSTagger/res/wiki/idxCorpus/"
//  val wikiIdxJson = "./res/wordIdx.json"
//  val rWikiIdxJson = "./res/rWordIdx.json"
//
//  val text = Source.fromFile(wikiIdxJson).mkString
//  val dictMap = parse(text).extract[Map[String,Int]]
//  val rDictMap = dictMap.map{case (k,v) => (v.toString,k)}
//  val pw = new PrintWriter(new BufferedWriter(new FileWriter(rWikiIdxJson)))
//    pw.println(compact(render(rDictMap)))
//    pw.close()
  //  dictMap ++= parse(text).extract[Map[String,Int]]
//
//  val wm = new WikiManager(path,thread = 4)
//  wm.ph.register(new LowerCaseProcessor)
//  wm.ph.register(new NumberProcessor)
//
//  def f(it:Iterator[List[String]],file:Path):Unit={
//    val fname = tPath + file.name
//    val pw = new PrintWriter(new BufferedWriter(new FileWriter(fname)))
//    for{sentence <- it}{
//      val line = sentence.map(wd=>{
//        dictMap.getOrElse(wd,{
//          if(wd.contains(NUMBERSTR))
//            RARENUMBERIDX
//          else
//            RAREIDX
//        })
//      }).mkString(",")
//      pw.println(line)
//    }
//    pw.close()
//  }
//  wm.map(path,f)



//  for{(str,idx)<- wikiIdx}{
//    println(s"$str: $idx")
//  }

//  val outputlim = 100
//  var sum = 0
//  var rarecounter = 0
//  val rIdx = JsonIdxReader.getRIdx()
//  val fname = "/home/et/IdeaProjects/POSTagger/res/wiki/idxCorpus/wiki_00.bz2.txt"
//  Source.fromFile(fname).getLines().foreach(str=>{
//    val strarr = str.split(",")
//    sum += strarr.size
//    rarecounter += strarr.count(idx=>{
//      val i = idx.toInt
//      i == RAREIDX || i == RARENUMBERIDX
//    })
//  })
//  println(s"Total: $sum, Rare: $rarecounter, Ratio:${sum.toDouble/rarecounter}")
}
