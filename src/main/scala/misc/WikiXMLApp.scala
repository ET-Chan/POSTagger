

/*
 * Created by ET.
 * You are free to distribute it and I do not what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package misc



import java.io._

import org.apache.commons.compress.compressors.bzip2.{BZip2CompressorOutputStream, BZip2CompressorInputStream}

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source
import scala.reflect.io.Path


object WikiXMLApp extends App{
  /*
  * This APP is for parsing the result from WikiParser.py
  * Mainly for removing the XML tag(doc). As it really does not matter
  * in our task
  * */
  val thread = 4
  val docTagR ="""^<doc[\s]+id="[\d]*"[\s]+url=".*"[\s]+title=".*">$""".r
  val docEndTagR="""^</doc>$""".r

  val srcPath = "/home/et/IdeaProjects/POSTagger/res/wiki/extracted/AA/"
  val targetPath = "/home/et/IdeaProjects/POSTagger/res/wiki/extractedTagOut/"
  val parWalker = Path(srcPath).walk.filter(_.path.endsWith("bz2")).toList.par
  parWalker.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(thread))
  parWalker.foreach(p=>{
    val bzbr = Source.fromInputStream(new BZip2CompressorInputStream(new FileInputStream(p.path))).getLines()
    val pw = new PrintWriter(new BufferedWriter(new FileWriter(targetPath+p.name+".txt")))

    bzbr.foreach(s=>{
      if(docTagR.findFirstIn(s).isEmpty &&
         docEndTagR.findFirstIn(s).isEmpty){//meaning: do not capture xml tag at all.
        pw.println(s)
      }
    })

    pw.close()
  })
}