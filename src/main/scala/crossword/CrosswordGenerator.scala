package crossword;

import java.net.URL
import java.text.Normalizer
import cspom.CSPOM
import CSPOM._
import cspom.variable.CSPOMVariable
import cspom.extension.MDD
import cspom.extension.ExtensionConstraint
import cspfj.generator.ProblemGenerator
import scala.util.Random
import scala.collection.mutable.HashMap

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
    val problem = crossword.generate();
    println(problem)
  }
}

class CrosswordGenerator(x: Int, y: Int, black: Set[Cell]) {
  val dicts = loadDicts(getClass.getResource("french"), math.max(x, y))
  val variables: Array[Array[CSPOMVariable]] = Array.ofDim(x, y)
  var map: Map[String, Cell] = Map.empty

  //val problem = new CSPOM;

  private def loadDicts(file: URL, max: Int) = {

    var dicts = new HashMap[Int, MDD]()

    val source = io.Source.fromURL(file)

    for (
      word <- source.getLines.map { l => Normalizer.normalize(l.toUpperCase(), Normalizer.Form.NFD).replaceAll("[^A-Z]", "") };
      if (word.length >= 2 && word.length <= max)
    ) {

      val ths = dicts.getOrElseUpdate(word.size, MDD.empty)

      val tuple = word map { c => c.toInt - 65 } toArray

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

  def generate() = {
    val problem = CSPOM {
      for (i <- 0 until x; j <- 0 until y) {
        if (!black.contains(Cell(i, j))) {
          variables(i)(j) = interVar(0, 25);
          map += variables(i)(j).name -> Cell(i, j)
        }
      }

      var currentWord: List[CSPOMVariable] = Nil

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

    ProblemGenerator.generate(problem);
  }

  private def newWord(word: Seq[CSPOMVariable])(implicit problem: CSPOM) {
    if (word.size >= 2) {
      ctr(dicts(word.size), false)(word: _*);
    }
  }

  def whatCell(varName: String) = map(varName)

}
