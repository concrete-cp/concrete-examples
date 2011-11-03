package crossword;

import cspfj.problem.Problem
import cspfj.MGACIter

class CrosswordResolver(problem: Problem) extends Thread {

  override def run() {
    val solver = new MGACIter(problem);

    // solver.setAllSolutions(true);

    if (solver.nextSolution() == null) {
      System.out.println("No crossword found");
    }
  }
}
