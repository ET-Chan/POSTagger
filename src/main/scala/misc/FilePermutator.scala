/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package misc

import java.io.{File, BufferedWriter, FileWriter, PrintWriter}
import java.nio.file.{Files, Paths}
import scala.collection.{AbstractIterator, AbstractIterable}
import scala.collection.mutable.ArrayBuffer
import scala.reflect.io.Path

import scala.io.Source
import scala.util.Random

/**
 * Created by et on 27/02/15.
 */
object FilePermutator extends App{
  Random.setSeed(System.currentTimeMillis())

  def shuffle(s:Path,t:Path)={
    val bw = new BufferedWriter(new FileWriter(t.path))
    bw.write(Random.shuffle(Source.fromFile(s.path).getLines()).mkString("\n"))
    bw.close()
  }
  def shuffleAll(path:Path)={
    val sPath = reflect.io.Path(path.toString)
    sPath.walk.foreach(p=>{
      val tmpFile = Path(Random.nextInt.toString)
      shuffle(new File(p.toString()),tmpFile)
      p.delete()
      Files.move(Paths.get(tmpFile.path),Paths.get(p.path))
    })
  }
  def merge(path:Path, shuffle:Boolean = true,chunks:Int,target:String)={
    val paths = new ArrayBuffer[Iterator[String]]()
  //  val bw = new BufferedWriter(new FileWriter(target))
    paths ++= path.walk.map(p=>Source.fromFile(p.path).getLines())
    val its = new AbstractIterator[String] {

      var nextline:String = null
      var res:Boolean = false
      fetchNext()
       def hasNext: Boolean = res

       def next(): String = {val ret = nextline;fetchNext();ret}

      private def fetchNext()={
        var fin = false
        while(!fin && paths.nonEmpty){
          val next = if (shuffle) Random.nextInt(paths.size) else 0
          if(paths(next).hasNext){
            val nextlinec = paths(next).next()
            if(!nextlinec.contentEquals("")){
              nextline = nextlinec
              fin = true
            }
          }else{
            paths(next) = paths(paths.size-1)
            paths.trimEnd(1)
          }
        }
        res = fin
      }

    }
    shatter(its,chunks,shuffle,target)

//    while(paths.nonEmpty){
//      val next = if (shuffle) Random.nextInt(paths.size) else 0
//      if(paths(next).hasNext) {
//        val nextline = paths(next).next()
//        if(!nextline.contentEquals("")){
//          bw.write(nextline+"\n")
//        }
//      }else{
//        paths(next) = paths(paths.size-1)
//        paths.trimEnd(1)
//      }
//    }


  }

  def shatter(path:Path,chunks:Int,shuffle:Boolean,targetPath:Path):Unit={
    assert(path.isFile)
    shatter(Source.fromFile(path.path).getLines(),chunks,shuffle,targetPath)
  }

  def shatter(it:Iterator[String],chunks:Int, shuffle:Boolean,targetPath:Path):Unit={

    val ab = new ArrayBuffer[String]()
//    val it = Source.fromFile(path.path).getLines()
    var counter = 1
    var no = 0
    var bw:BufferedWriter = null
    for{line<- it
        if !line.contentEquals("")} {
        if (counter % chunks == 0){
            counter = 0
            if(shuffle) Random.shuffle(ab)
            bw = new BufferedWriter(new FileWriter(targetPath.path+no))
            bw.write(ab.mkString("\n"))
            bw.close()
            no += 1
            ab.clear()
          }
          ab += line
    //      bw.write(line+"\n")
          counter += 1
      }
        if(ab.nonEmpty) {
          if (shuffle) Random.shuffle(ab)
          bw = new BufferedWriter(new FileWriter(targetPath.path+no))
          bw.write(ab.mkString("\n"))
          bw.close()
        }
  }

//  val path = "./res/wiki/idxCorpusBackup"
//  merge(path,false)
//  shatter(path,2000000)
//  Path(path).delete()
//  shuffleAll("./res/wiki/idxCorpus")
//  shatter("res/merge.txt",2000000,true,"./res/wiki/idxCorpus/wiki")
  merge("./res/wiki/idxCorpusBackup/",true,500000,"./res/wiki/idxCorpus/wiki")

}
