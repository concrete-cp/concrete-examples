package crossword;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.filter.AC3Constraint;
import cspfj.problem.Problem;

public class CrosswordResolver extends Thread {

	private final Problem problem;

	public CrosswordResolver(Problem problem) {
		this.problem = problem;
	}

	public void run() {
		final Solver solver = new MGACIter(problem);

		// solver.setAllSolutions(true);

		if (solver.nextSolution() == null) {
			System.out.println("No crossword found");
		}
	}
}
