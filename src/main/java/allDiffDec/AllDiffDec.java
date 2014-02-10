package allDiffDec;

import concrete.JCSPOMDriver;
import static concrete.JCSPOMDriver.*;
import concrete.Solver;
import concrete.SolverResult;
import concrete.generator.FailedGenerationException;
import cspom.CSPOM;
import cspom.variable.IntVariable;

public final class AllDiffDec {
	private AllDiffDec(final int size) {
	}

	public static CSPOM generate() {
		final JCSPOMDriver problem = new JCSPOMDriver();

		final IntVariable x1 = interVar("X1", 3, 4);
		final IntVariable x2 = interVar("X2", 1, 5);
		final IntVariable x3 = interVar("X3", 3, 4);
		final IntVariable x4 = interVar("X4", 2, 5);
		final IntVariable x5 = interVar("X5", 1, 1);

		problem.ctr(problem.allDifferent(x1, x2, x3, x4, x5));
		return problem;
	}

	public static void main(String[] args) throws FailedGenerationException {
		final CSPOM problem = generate();

		final Solver solver = Solver.apply(problem);

		SolverResult solution = solver.nextSolution();
		while (solution.isSat()) {
			System.out.println(solution);
			solution = solver.nextSolution();
		}

	}
}
