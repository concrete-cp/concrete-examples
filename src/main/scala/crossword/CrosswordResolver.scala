package crossword;

import cspfj.Problem
import cspfj.MAC

class CrosswordResolver(problem: Problem) extends Thread {

  override def run() {
    val solver = new MAC(problem);

    // solver.setAllSolutions(true);

    if (solver.nextSolution().isSat) {
      println("Crossword found")
    } else {
      println("No crossword found");
    }
  }
}
