package crossword;

import cspfj.problem.Problem
import cspfj.MGACIter

class CrosswordResolver(problem: Problem) extends Thread {

  override def run() {
    val solver = new MGACIter(problem);

    // solver.setAllSolutions(true);

    if (solver.nextSolution().isDefined) {
      println("Crossword found")
    } else {
      println("No crossword found");
    }
  }
}
