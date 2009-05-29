package crossword;

import java.io.IOException;

import cspfj.AbstractSolver;
import cspfj.MGACRec;
import cspfj.ResultHandler;
import cspfj.Solver;
import cspfj.filter.DC1;
import cspfj.problem.Problem;

public class CrosswordResolver extends Thread {
	
	private final Problem problem;
	
	public CrosswordResolver(Problem problem) {
		this.problem = problem;
	}
	
	public void run() {
		final Solver solver = new MGACRec(problem, new ResultHandler());
		
		//solver.setUsePrepro(CDC.class);
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
