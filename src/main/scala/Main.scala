/**
 * Created by et on 1/31/15.
 */

import java.io.{FileInputStream, ObjectInputStream, ObjectOutputStream, FileOutputStream}
import java.util.logging.Logger

import Utils._

import scala.io.StdIn
import scala.reflect.io.Path

object Main extends App{
  val iter = 10
  val ratio = 0.9
//  CorpusManager.combineAll("res/WSJ-2-12","./res/outcorp")
//  p.parse("""/home/et/IdeaProjects/POSTagger/res/WSJ-2-12/02/WSJ_0200.POS""").head.foreach(println(_))

  val p = new Parser()
////

//  val r:Seq[Double] = for(i<- 0 until iter) yield {
//      CorpusManager.split(ratio,"res/outcorp","res/lCorp","res/tCorp")
//      val h = new HMM()
//      h.learn(p.parse("res/lCorp").flatten,p.parse("res/lCorp").flatten)
//      println(s"Dealing with $i")
//      h.validate(p.parse("res/tCorp"))
//  }
//  var sum=0.0
//  for(i<- 0 until r.size){
//    sum+=r(i)
//    println(r(i))
//  }
//  println(s"Avg: ${sum/r.size}")
//
//  CorpusManager.reportAllException("/home/et/IdeaProjects/POSTagger/res/WSJ-2-12/")
//
//    val r:Seq[Double] = for(i<- 0 until iter) yield {
//        CorpusManager.split(ratio,"res/outcorp","res/lCorp","res/tCorp")
//        val h = new HMMTrig()
//        h.learn(p.parse("res/lCorp").flatten,p.parse("res/lCorp"))
//        println(s"Learned complete with $i")
//        h.validate(p.parse("res/tCorp"))
//    }
//    var sum=0.0
//    for(i<- 0 until r.size){
//      sum+=r(i)
//      println(r(i))
//    }
//    println(s"Avg: ${sum/r.size}")

//        StdIn.readLine()
    val h = new HMMTrigKatz()
    h.learn(p.parse("res/lCorp").flatten,p.parse("res/lCorp"))
    h.save
//      val h = new HMMTrigKatz()
//      h.load
//      println(h.validate(p.parse("res/tCorp")))
//    println(h.predict(Seq(STARTSTR,"I","do","believe","that","he","is","an","idiot",".",STOPSTR).toStream).mkString(" "))
}
