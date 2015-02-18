/**
 * Created by et on 1/31/15.
 */

import java.io.{FileInputStream, ObjectInputStream, ObjectOutputStream, FileOutputStream}
import java.nio.file.{Paths, Files}
import java.util.logging.Logger

import Utils._

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.StdIn
import scala.reflect.io.{Directory, Path}
import scala.util.Random

object Main extends App{
  val iter = 8
  val ratio = 0.9
  val thread = 4
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

  val params = Array(0)


  for(d<- params) {
//  StdIn.readLine()
//   val prev = System.currentTimeMillis()


    val r = range.map(i=> {
      val lCorp = "res/lCorp" + Random.nextInt()
      val tCorp = "res/tCorp" + Random.nextInt()
      CorpusManager.split(ratio, "res/outcorp", lCorp, tCorp)
      val h = new HMMTrigKNM()
      h.printIters = Int.MaxValue
      h.learn(p.parse(lCorp).flatten, p.parse(lCorp))
//      println(s"Learned complete with $i")
      val ret = h.validate(p.parse(tCorp))
      Files.delete(Paths get lCorp)
      Files.delete(Paths get tCorp)
      ret
    })
    val sum = r.sum
//    println (s"Time: ${(System.currentTimeMillis - prev)}")
    println(s"Params: ${d}, Avg: ${sum/ r.size}")
  }

//        StdIn.readLine()
//    val h = new HMMTrigKatz()
//    h.learn(p.parse("res/lCorp").flatten,p.parse("res/lCorp"))
//    h.save
//      val h = new HMMTrigKatz()
//      h.load
//    println(h.predict(Seq(STARTSTR,"you","are","a","bastard",".",STOPSTR).toStream).mkString(" "))
//        println(h.validate(p.parse("res/tCorp")))

}
