/**
 * Created by et on 1/31/15.
 */

import java.io.{FileInputStream, ObjectInputStream, ObjectOutputStream, FileOutputStream}
import java.nio.file.{Paths, Files}
import java.util.logging.Logger

import misc.{Parser, CorpusManager, Utils}
import Utils._
import bigram._
import trigram._

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.StdIn
import scala.reflect.io.{Directory, Path}
import scala.util.Random

object Main extends App{
  val iter = 12
  val ratio = 0.9
  val thread = 4
//  CorpusManager.combineAll("res/WSJ-2-12","./res/outcorp")
//  p.parse("""/home/et/IdeaProjects/POSTagger/res/WSJ-2-12/02/WSJ_0200.POS""").head.foreach(println(_))

  val p = new Parser()
////

//  val r:Seq[Double] = for(i<- 0 until iter) yield {
//      CorpusManager.split(ratio,"res/outcorp","res/lCorp","res/tCorp")
//      val h = new bigram.HMM()
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
  Runtime.getRuntime.addShutdownHook(new Thread(){
    override def run(): Unit ={
      val p = Directory("./res")
      p.files.foreach(f=>{
        if(f.toString.contains("tCorp") || f.toString().contains("lCorp"))
          f.delete()
      })
    }
  })

  val range = (0 until iter).par
  range.tasksupport = new ForkJoinTaskSupport(
    new ForkJoinPool(thread))

  val params = Array(4)
//  val lCorp = "res/lCorp" + Random.nextInt()
//  val tCorp = "res/tCorp" + Random.nextInt()
//  CorpusManager.split(ratio, "res/outcorp", lCorp, tCorp)

  for(d<- params) {
//  StdIn.readLine()
//   val prev = System.currentTimeMillis()


    val r = range.map(i=> {
      val lCorp = "res/lCorp" + Random.nextInt()
      val tCorp = "res/tCorp" + Random.nextInt()
      CorpusManager.split(ratio, "res/outcorp", lCorp, tCorp)
      val hknm = new HMMTrigKNM
      val hkn = new HMMTrigKN
      val hwb = new HMMTrigWB
      val hkz = new HMMTrigKatz
      val h = new HMMTrig
//      h.printIters = Int.MaxValue
      h.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hkz.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hwb.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hknm.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hkn.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      println(s"Learned complete with $i")
      val r = h.validate(p.parse(tCorp))
      val rkz = hkz.validate(p.parse(tCorp))
      val rwb = hwb.validate(p.parse(tCorp))
      val rknm = hknm.validate(p.parse(tCorp))
      val rkn = hkn.validate(p.parse(tCorp))
      Files.delete(Paths get lCorp)
      Files.delete(Paths get tCorp)
      (r,rkz,rwb,rknm,rkn)
    })
//    val sum = r.sum
//    println (s"Time: ${(System.currentTimeMillis - prev)}")
//    println(s"Params: ${d}, Avg: ${sum/ r.size}")

    println(s"Ada:${r.map(_._1).sum/r.size},Katz:${r.map(_._2).sum/r.size},WB:${r.map(_._3).sum/r.size},KNM:${r.map(_._4).sum/r.size},KN:${r.map(_._5).sum/r.size}")
  }

//        StdIn.readLine()
//    val h = new trigram.HMMTrigKatz()
//    h.learn(p.parse("res/lCorp").flatten,p.parse("res/lCorp"))
//    h.save
//      val h = new trigram.HMMTrigKatz()
//      h.load
//    println(h.predict(Seq(STARTSTR,"you","are","a","bastard",".",STOPSTR).toStream).mkString(" "))
//        println(h.validate(p.parse("res/tCorp")))

}
