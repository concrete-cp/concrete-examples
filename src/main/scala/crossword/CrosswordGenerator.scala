package crossword;

import java.net.URL
import java.text.Normalizer

import cspom.CSPOM
import cspom.variable.CSPOMVariable
import cspom.extension.Relation
import cspom.extension.ExtensionConstraint
import cspfj.generator.ProblemGenerator

/*
 * Created on 20 mai 08
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

class CrosswordGenerator(x: Int, y: Int, black: Set[Cell]) {
  val dicts = loadDicts(getClass.getResource("french"), math.max(x, y))
  val variables: Array[Array[CSPOMVariable]] = Array.ofDim(x, y)
  var map: Map[String, Cell] = Map.empty

  val problem = new CSPOM;

  private def loadDicts(file: URL, max: Int) = {

    var dicts: Map[Int, Relation] = Map.empty

    val source = io.Source.fromURL(file)

    for (
      word <- source.getLines.map { l => Normalizer.normalize(l.toUpperCase(), Normalizer.Form.NFD).replaceAll("[^A-Z]", "") };
      if (word.length >= 2 && word.length <= max)
    ) {

      val ths = dicts.get(word.size) match {
        case None => {
          val t = new Relation(word.size)
          dicts += word.size -> t
          t
        }
        case Some(t) => t
      }

      val tuple = word map { c => c.toInt - 65 } toArray

      ths.addTuple(tuple);

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

    for (i <- 0 until x; j <- 0 until y) {
      if (!black.contains(Cell(i, j))) {
        variables(i)(j) = problem.interVar(0, 25);
        map += variables(i)(j).name -> Cell(i, j)
      }
    }

    var currentWord: List[CSPOMVariable] = Nil

    for (i <- 0 until x) {
      for (j <- 0 until y) {
        if (variables(i)(j) == null) {
          newWord(currentWord);
          currentWord = Nil
        } else {
          currentWord ::= variables(i)(j);
        }
      }
      newWord(currentWord);
      currentWord = Nil
    }

    for (j <- 0 until y) {
      for (i <- 0 until x) {
        if (variables(i)(j) == null) {
          newWord(currentWord);
          currentWord = Nil
        } else {
          currentWord ::= variables(i)(j);
        }
      }
      newWord(currentWord);
      currentWord = Nil
    }

    ProblemGenerator.generate(problem);
  }

  private def newWord(word: Seq[CSPOMVariable]) {
    if (word.size >= 2) {
      problem.addConstraint(new ExtensionConstraint(dicts(word.size), false, word));
    }
  }

  def whatCell(varName: String) = map(varName)

}
