/**
 * Created by et on 1/31/15.
 */
object Main extends App{
//  CorpusManager.combineAll("res/","./outcorp")
  val p = new Parser()
  val h = new HMM()
//  p.parse("""/home/et/IdeaProjects/POSTagger/res/WSJ-2-12/02/WSJ_0200.POS""").head.foreach(println(_))
//    CorpusManager.split(0.9,"outcorp","lCorp","tCorp")

  h.learnEmission( p.parse("tCorp").flatten.toSeq)
  h.printEmission
}
