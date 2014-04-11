package crossword;

import concrete.Problem
import concrete.MAC
import concrete.ParameterManager

class CrosswordResolver(problem: Problem, pm: ParameterManager) extends Thread {

  override def run() {
    val solver = new MAC(problem, pm);

    // solver.setAllSolutions(true);

    if (solver.nextSolution().isSat) {
      println("Crossword found")
    } else {
      println("No crossword found");
    }
  }
}
