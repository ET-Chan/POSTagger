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
  val iter = 12 //iter-folded validation
  val ratio = 0.9//sampling with this ratio, meaning 90% of the data will be the training set, the remain will be test set.
  val thread = 4//use all four threads

  val p = new Parser()

  //Better life starts from tidying up all left over files of every training and testing.
  Runtime.getRuntime.addShutdownHook(new Thread(){
    override def run(): Unit ={
      val p = Directory("./res")
      p.files.foreach(f=>{
        if(f.toString.contains("tCorp") || f.toString().contains("lCorp"))
          f.delete()
      })
    }
  })

  //range is to initialize the multi-threaded validation
  val range = (0 until iter).par
  range.tasksupport = new ForkJoinTaskSupport(
    new ForkJoinPool(thread))

  val params = Array(4)//parameters, this is used for testing which parameters of smoothing is the best, but it is not used currently.

  for(d<- params) {//for every parameters
//  StdIn.readLine()
//   val prev = System.currentTimeMillis()


    val r = range.map(i=> {//we iter-folded validate the model
      val lCorp = "res/lCorp" + Random.nextInt()//generate a random filename, used by different threads
      val tCorp = "res/tCorp" + Random.nextInt()//...
      CorpusManager.split(ratio, "res/outcorp", lCorp, tCorp)//split the file by random sampling
      val hknm = new HMMKNM//testing five different smoothing altogether, with the same dataset
      val hkn = new HMMKN
      val hwb = new HMMWB
      val hkz = new HMMKatz
      val h = new HMM
//      h.printIters = Int.MaxValue
      //learn all the models
      h.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hkz.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hwb.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hknm.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      hkn.learn(p.parse(lCorp).flatten, p.parse(lCorp))
      println(s"Learned complete with $i")
      //and validate all of them
      val r = h.validate(p.parse(tCorp))//
      val rkz = hkz.validate(p.parse(tCorp))
      val rwb = hwb.validate(p.parse(tCorp))
      val rknm = hknm.validate(p.parse(tCorp))
      val rkn = hkn.validate(p.parse(tCorp))
      //cleaning
      Files.delete(Paths get lCorp)
      Files.delete(Paths get tCorp)
      //return their results
      (r,rkz,rwb,rknm,rkn)
    })
//    val sum = r.sum
//    println (s"Time: ${(System.currentTimeMillis - prev)}")
//    println(s"Params: ${d}, Avg: ${sum/ r.size}")

    println(s"Ada:${r.map(_._1).sum/r.size},Katz:${r.map(_._2).sum/r.size},WB:${r.map(_._3).sum/r.size},KNM:${r.map(_._4).sum/r.size},KN:${r.map(_._5).sum/r.size}")
//    println(s"Niave:${r.sum/r.size}")

  }


}
