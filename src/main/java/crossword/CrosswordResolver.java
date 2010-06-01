package crossword;

import java.io.IOException;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.problem.Problem;

public class CrosswordResolver extends Thread {

	private final Problem problem;

	public CrosswordResolver(Problem problem) {
		this.problem = problem;
	}

	public void run() {
		final Solver solver = new MGACIter(problem);

		// solver.setAllSolutions(true);
		try {
			if (solver.nextSolution() == null) {
				System.out.println("No crossword found");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
