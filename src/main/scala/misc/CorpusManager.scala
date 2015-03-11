package misc

import java.io.{FileWriter, BufferedWriter, PrintWriter}
import java.util.logging.{Level, Logger}
import org.json4s._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import preprocessor.{NumberProcessor, LowerCaseProcessor, ProcessorHub}
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.reflect.io.Path
import scala.util.Random
import misc.Utils._

/**
 * Created by et on 1/31/15.
 */
object CorpusManager {
  /*
  * This object is used for manging the corpus provided
  * */
  val log = Logger.getLogger("CM")
  def combineAll(p:Path,t:Path)={
    /*
     *combine all the data inside p
     * and output them as a single file to t
    */
    val pw = new PrintWriter(t.path)
    val s = p.walk.filter(_.path.toLowerCase.endsWith("pos")).map(p=>Source.fromFile(p.path).mkString).foreach(pw.println(_))
    pw.close()
  }
  val DefaultDelim = "==============================================\n"
  def split(prob:Double,s:Path,l:Path,r:Path,d:String = DefaultDelim)= {
    /*
    * Split the single file corpus, to two files
    * with the ratio params,
    * prob*|s| data will goto l, while the remain will goto r
    * */
    val p = new Parser()
    assert(prob<1.0 && prob>0.0)
    val rand = new Random(System.currentTimeMillis())
    val ss = p.split(Source.fromFile(s.path).getLines().toStream,"""=+""")
    val lpw = new PrintWriter(l.path)
    val rpw = new PrintWriter(r.path)
    ss.foreach(s=>{
      if(rand.nextDouble()<prob)
        lpw.println(DefaultDelim+s)
      else rpw.println(DefaultDelim+s)
    })
    lpw.close();rpw.close()
  }
  def reportAllException(p:Path)={
    /*
    * It is for cleaning the dataset
    * abandoned currently.
    * */
    log.info("HELLO")
    val par = new Parser
    try {
      p.walk.filter(_.path.toLowerCase.endsWith("pos")).flatMap(par.parse).flatten.foreach(q=>{val k = q._2})
    }catch{
      case pe:ParseException=>{
        val errPath = p.walk.toStream
          .filter(_.path.toLowerCase.endsWith("pos"))
          .filter(pp=>Source.fromFile(pp.path).mkString.contains(
            pe.lineStr
          ))
          .head
        log.log(Level.INFO,s"At ${errPath}, line ${pe.lineStr}, parsing error")
      }
    }
  }

  def convertToLua(p:Path,t:Path)={
    /*
    * This is to convert the data in provided form
    * to lua form, for NN learning
    * Each sentence will be transformed to three lines
    * the first one is  sentence word indicies
    * the second line is the tag indicies
    * the third line is the one-hot vector, encoding the capitialization.
    * */

    assert(p.isFile)
    implicit val formats = DefaultFormats
    val wordmapname = "./res/wordIdx.json"
    val tagmapname = "./res/tagmap.json"

    val wordmap = parse(Source.fromFile(wordmapname).mkString).extract[Map[String,Int]]
    val tagmap = parse(Source.fromFile(tagmapname).mkString).extract[Map[String,Int]]

    val ph = new ProcessorHub
    ph.register(new LowerCaseProcessor)
    ph.register(new NumberProcessor)

    def wordmaphelper(word:String):Int={
      wordmap.getOrElse(word,{
        if(word.contains("NUMBER"))
          RARENUMBERIDX
        else
          RAREIDX
      })
    }
    def capitalhelper(word:String):Int={
      //treat words with NUMBER the same
      //I am lazy to deal with that.
      if(word.forall(_.isLower))
        return CAPITAL_ALL_LOW
      if(word.forall(_.isUpper))
        return CAPITAL_ALL_UP
      if(word(0).isUpper)
        return CAPITAL_FIRST_UP

      CAPITAL_ANY_UP
    }

    def flatten(s:Stream[Token]):Stream[Stream[Token]]={
      /*
      * the name somehow is misleading
      * this is not flatten, instead, it is to process the words with
      * multiple tags
      * Say for example.
      * If a sentence have tags like, DT, JJ|NNP, NNP
      * then this method will generate
      * [[DT,JJ,NNP],[DT,NNP,NNP]]
      * */
      val (pre,suff) = s.span{case (_,tag)=> !tag.contains("|")}
      var ret:Stream[Stream[Token]] = null
      if(suff.isEmpty)
        ret = Stream(pre)
      else{
        val ambs = suff.head._2.split('|')
        for{amb<- ambs}{
          val res = flatten((suff.head._1,amb) #:: suff.drop(1))
          ret = res.map(suffs => {
            pre #::: suffs
          })
        }

      }
      ret
    }

    //one sentence represented by two lines
    //first line is the word idx associated with
    //second line is the tag idx
    val pw = new PrintWriter(new BufferedWriter(new FileWriter(t.path)))
    val par = new Parser
    par.lowercase = false
    par.parse(p).flatMap(s=>flatten(s.toStream)).foreach(s=>{
      val (words,tags,capitals) = s.drop(1).dropRight(1).map{case (str,tag)=> {
        if(tag == STOPTAG){
          println("ERROR@!!")
        }
        (wordmaphelper(ph.parse(str)), tagmap(tag), capitalhelper(str))
      }}.unzip3
      pw.println(words.mkString(","))
      pw.println(tags.mkString(","))
      pw.println(capitals.mkString(","))
    })

    pw.close()

  }

}
