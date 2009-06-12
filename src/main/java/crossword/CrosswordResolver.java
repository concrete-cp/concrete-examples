package crossword;

import java.io.IOException;

import cspfj.AbstractSolver;
import cspfj.ResultHandler;
import cspfj.Solver;
import cspfj.filter.DC2;
import cspfj.ls.Tabu;
import cspfj.problem.Problem;

public class CrosswordResolver extends Thread {
	
	private final Problem problem;
	
	public CrosswordResolver(Problem problem) {
		this.problem = problem;
	}
	
	public void run() {
		final Solver solver = new Tabu(problem, new ResultHandler(), false);
		
		solver.setUsePrepro(DC2.class);
		AbstractSolver.parameter("cdc.addConstraints", "BIN");
		// solver.setAllSolutions(true);
		try {
			if (!solver.runSolver()) {
				System.out.println("No crossword found");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
