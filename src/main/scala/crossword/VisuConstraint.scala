package crossword

import java.awt.Color

import scala.collection.BitSet

import cspfj.constraint.AbstractConstraint
import cspfj.constraint.VariableGrainedRemovals
import cspfj.filter.RevisionHandler
import cspfj.problem.Variable

class VisuConstraint(variables: Array[Variable], crossword: CrosswordGui)
  extends AbstractConstraint(null, variables)
  with VariableGrainedRemovals {

  // private int level;
  var modified = (0 until variables.length) map (_ => BitSet.empty)
  val cell = crossword.cell

  private def setCell(name: String, value: Int) {
    var varCell = crossword.crossword.whatCell(name);
    if (value < 0) {
      cell(varCell.x)(varCell.y).setText("");
    } else {
      cell(varCell.x)(varCell.y).setText(String
        .valueOf((value + 65).toChar));
    }
    cell(varCell.x)(varCell.y).setForeground(Color.getHSBColor(2f * level, 1, 1));
  }

  private def setCellNum(name: String, value: Int) {
    val varCell = crossword.crossword.whatCell(name);
    if (value < 0) {
      cell(varCell.x)(varCell.y).setText("");
    } else {
      cell(varCell.x)(varCell.y).setText(String.valueOf(value));
    }
    cell(varCell.x)(varCell.y).setForeground(Color.getHSBColor(2f * level, 1, 1));
  }

  override def check = true

  override def getEvaluation = Integer.MAX_VALUE

  override def level_=(newLevel: Int) {
	 
    if (newLevel < level) {
      for (i <- modified(newLevel)) {
        val v = scope(i);
        if (v.dom.size == 1) {
          setCell(v.name, v.dom.firstValue)
        } else {
          setCellNum(v.name, v.dom.size)
        }
      }
      modified = modified.updated(newLevel, BitSet.empty)
    }
    super.level = newLevel
  }

  def revise(revisator: RevisionHandler, reviseCount: Int) = {
    for ((v, p) <- varsWithRemovals(reviseCount)) {
      modified = modified.updated(level, modified(level) + p);
      if (v.dom.size == 1) {
        setCell(v.name, v.dom.firstValue);
      } else {
        setCellNum(v.name, v.dom.size);
      }
    }

    true;
  }
}