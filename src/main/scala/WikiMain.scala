import java.io.{FileInputStream, ObjectInputStream, ObjectOutputStream, FileOutputStream}
import java.util.concurrent.ConcurrentHashMap

import edu.stanford.nlp.ling.{Word, HasWord}
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory
import edu.stanford.nlp.process.{PTBTokenizer, TokenizerFactory, DocumentPreprocessor}

import misc.{NumberProcessor, Parser}
import misc.Utils._

import scala.collection.concurrent.TrieMap
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source
import scala.reflect.io.Path
import scala.collection.JavaConversions._

/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */
object WikiMain extends App {
  val thread = 1
//
//  val ctmap = new TrieMap[String,Int]()
//
  val path = "/home/et/IdeaProjects/POSTagger/res/wiki/extractedTagOut"
  val parWalker = Path(path).walk.filter(_.path.endsWith("txt")).toList.par
  val np = new NumberProcessor
//  parWalker.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(thread))
  parWalker.foreach(p=>{
    val dp = new DocumentPreprocessor(Source.fromFile(p.path).bufferedReader())

    val ptOpts = "normalizeParentheses=false,normalizeOtherBrackets=false"
    dp.setTokenizerFactory(PTBTokenizerFactory.newWordTokenizerFactory(ptOpts))


    for{sentence <- dp.iterator()}{

      sentence.foreach(hw=>{
        val str = np.parse(hw.word().toLowerCase)
        if(str.contains(NUMBERSTR))
          println(s"$hw->$str : $sentence")
//        ctmap.putIfAbsent(str,0)
//        ctmap(str) += 1
      })
    }
  })
//
//  val statsPath = "./res/wikistats.dat"
//  val oos = new ObjectOutputStream(new FileOutputStream(statsPath))
//  oos.writeObject(ctmap)
//  oos.close()

//    val ois = new ObjectInputStream(new FileInputStream(statsPath))
//    val ctlist = ois
//      .readObject()
//      .asInstanceOf[TrieMap[String,Int]]
//      .toList
//      .sortBy(_._2)
//      .reverse
//    val outputLimit = 100
//    var counter = 0
//    for{(str, freq)<- ctlist}{
//      println(s"$str: $freq")
//      counter += 1
//      if(counter > outputLimit)
//        System.exit(0)
//    }
//  val ctmap = ois
//    .readObject()
//    .asInstanceOf[TrieMap[String,Int]]
//  println(s"Size of ctmap: ${ctmap.size}")
  //existence test, to check if there is anything in outCorp not exist on the gigantic library now
//  val spCorpus = "/home/et/IdeaProjects/POSTagger/res/outcorp"
//  val p = new Parser()
//  p.parse(spCorpus).flatten.map(_._1).foreach(str=>{
//    if(str!=STARTSTR && str!=STOPSTR && ctmap.get(str).isEmpty){
//      println(s"Missing: $str")
//    }
//  })

}
