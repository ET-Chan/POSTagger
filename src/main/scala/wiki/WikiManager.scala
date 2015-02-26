/*
 * Created by ET.
 * You are free to distribute it and I do not know what license should I use.
 * TODO: Investigate which License is the best for school work source code.
 * Copyleft (c) 2015.
 */

package wiki

import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.process.DocumentPreprocessor
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory
import preprocessor.{ProcessorHub, NumberProcessor}

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source
import scala.reflect.io.Path
import scala.collection.JavaConversions._

/**
 * Created by et on 26/02/15.
 */
class WikiManager(path:String,
                  extension:String = "txt",
                  thread:Int = Runtime.getRuntime.availableProcessors()){

  val parWalker = Path(path).walk.filter(_.path.endsWith(extension)).toList.par
  parWalker.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(thread))

  val ph = new ProcessorHub


  var ptOpts = "normalizeParentheses=false,normalizeOtherBrackets=false"

  def map[A](t:Path,f: (Iterator[List[String]],Path)=>A)={
    parWalker.foreach(p=>{
      val dp = new DocumentPreprocessor(Source.fromFile(p.path).bufferedReader())

      dp.setTokenizerFactory(PTBTokenizerFactory.newWordTokenizerFactory(ptOpts))

      val dpit = dp
        .iterator()
        .map(e=>{
          e.toList.map(
            wd => {
              ph.parse(wd.word)
            })})

      f(dpit,p)
      }
    )
  }
}
