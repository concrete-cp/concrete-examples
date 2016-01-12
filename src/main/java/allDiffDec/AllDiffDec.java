package allDiffDec;

import static cspom.JCSPOM.intVarRange;
import scala.collection.Map;
import concrete.CSPOMSolver;
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

		final IntVariable x1 = p.nameExpression(intVarRange(3, 4), "X1");
		final IntVariable x2 = p.nameExpression(intVarRange(1, 5), "X2");
		final IntVariable x3 = p.nameExpression(intVarRange(3, 4), "X3");
		final IntVariable x4 = p.nameExpression(intVarRange(2, 5), "X4");
		final IntVariable x5 = p.nameExpression(intVarRange(1, 1), "X5");

		p.ctr(p.allDifferent(x1, x2, x3, x4, x5));
		return p;
	}

	public static void main(String[] args) throws FailedGenerationException {
		final CSPOM problem = generate();

		final CSPOMSolver solver = Solver.apply(problem).get();

		while (solver.hasNext()) {
			Map<String, Object> solution = solver.next();
			System.out.println(solution);
		}

	}
}
