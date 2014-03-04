package allDiffDec;

import static cspom.CSPOM.interVar;
import concrete.JCSPOMDriver;
import concrete.Solver;
import concrete.SolverResult;
import concrete.generator.FailedGenerationException;
import cspom.CSPOM;
import cspom.variable.IntVariable;

public final class AllDiffDec {
	private AllDiffDec(final int size) {
	}

	public static CSPOM generate() {
		final JCSPOMDriver p = new JCSPOMDriver();

		final IntVariable x1 = p.nameExpression(interVar(3, 4), "X1");
		final IntVariable x2 = p.nameExpression(interVar(1, 5), "X2");
		final IntVariable x3 = p.nameExpression(interVar(3, 4), "X3");
		final IntVariable x4 = p.nameExpression(interVar(2, 5), "X4");
		final IntVariable x5 = p.nameExpression(interVar(1, 1), "X5");

		p.ctr(p.allDifferent(x1, x2, x3, x4, x5));
		return p;
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
