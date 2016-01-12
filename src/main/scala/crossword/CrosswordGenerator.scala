package crossword;

import java.net.URL
import java.text.Normalizer
import cspom.CSPOM
import CSPOM._
import cspom.variable.CSPOMVariable
import cspom.extension.MDD
import concrete.generator.ProblemGenerator
import scala.util.Random
import scala.collection.mutable.HashMap
import cspom.variable.IntVariable
import concrete.ParameterManager

/*
 * Created on 20 mai 08
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

object CrosswordGenerator {
  def main(args: Array[String]) {
    Thread.sleep(30)
    val x = 10
    val y = 10
    val RAND = new Random(1)
    val black = (for (i <- 0 until x; j <- 0 until y; if RAND.nextFloat < .15) yield Cell(i, j)).toSet
    //Set(Cell(4,4))
    val crossword = new CrosswordGenerator(x, y, black);
    val problem = crossword.generate(new ParameterManager);
    println(problem)
  }
}

class CrosswordGenerator(x: Int, y: Int, black: Set[Cell]) {
  val dicts = loadDicts(getClass.getResource("french"), math.max(x, y))
  val variables: Array[Array[IntVariable]] = Array.ofDim(x, y)
  var map: Map[String, Cell] = Map.empty

  //val problem = new CSPOM;

  private def loadDicts(file: URL, max: Int) = {

    var dicts = new HashMap[Int, MDD[Int]]()

    val source = io.Source.fromURL(file)

    for (
      word <- source.getLines.map { l => Normalizer.normalize(l.toUpperCase(), Normalizer.Form.NFD).replaceAll("[^A-Z]", "") };
      if (word.length >= 2 && word.length <= max)
    ) {

      val ths = dicts.getOrElseUpdate(word.size, MDD.empty)

      val tuple = word map { c => c.toInt - 65 }

      dicts += word.size -> (ths + tuple);

    }
    dicts;
  }

  def transpose[E](matrix: Array[Array[E]], transposed: Array[Array[E]]) = {
    for (i <- matrix.indices) {
      for (j <- matrix(i).indices) {
        transposed(j)(i) = matrix(i)(j);
      }
    }
    transposed
  }

  def generate(pm: ParameterManager) = {
    val problem = CSPOM { implicit problem =>
      for (i <- 0 until x; j <- 0 until y) {
        if (!black.contains(Cell(i, j))) {
          val name = s"C$i.$j"
          variables(i)(j) = IntVariable(0 to 25) as name
          map += name -> Cell(i, j)
        }
      }

      var currentWord: List[IntVariable] = Nil

      for (i <- 0 until x) {
        for (j <- 0 until y) {
          if (variables(i)(j) == null) {
            newWord(currentWord.reverse);
            currentWord = Nil
          } else {
            currentWord ::= variables(i)(j);
          }
        }
        newWord(currentWord.reverse);
        currentWord = Nil
      }

      for (j <- 0 until y) {
        for (i <- 0 until x) {
          if (variables(i)(j) == null) {
            newWord(currentWord.reverse);
            currentWord = Nil
          } else {
            currentWord ::= variables(i)(j);
          }
        }
        newWord(currentWord.reverse);
        currentWord = Nil
      }
    }

    new ProblemGenerator(pm).generate(problem);
  }

  private def newWord(word: Seq[IntVariable])(implicit problem: CSPOM) {
    if (word.size >= 2) {
      ctr(word in dicts(word.size))
    }
  }

  def whatCell(varName: String) = map(varName)

}
