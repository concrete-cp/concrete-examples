import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import concrete.IntDomain;
import concrete.MAC;
import concrete.Problem;
import concrete.SolverResult;
import concrete.Variable;
import concrete.constraint.semantic.Neq;

import scala.collection.immutable.Range;
import concrete.constraint.extension.*;

public class TestCspfj {
	public static void main(String[] args) throws IOException {
		Logger.getLogger("").setLevel(Level.WARNING);
		Problem problem = new Problem();

		concrete.Variable v0 = problem.addVariable("V0", IntDomain.apply(new int[] { 0, 1, 2 }));
		concrete.Variable v1 = problem.addVariable("V1", IntDomain.apply(new Range(0, 3, 1)));
		concrete.Variable v2 = problem.addVariable("V2", IntDomain.apply(new Range(0, 3, 1)));
		concrete.Variable v3 = problem.addVariable("V3", IntDomain.apply(new Range(0, 3, 1)));

		final ExtensionConstraint noGoodsConstraint = BinaryExt.apply(new Variable[] { v0, v1 },
				new Matrix2D(3, 3, true), false);

		problem.addConstraint(noGoodsConstraint);

		problem.addConstraint(new Neq(v0, v2));
		problem.addConstraint(new Neq(v0, v1));
		problem.addConstraint(new Neq(v0, v3));
		problem.addConstraint(new Neq(v1, v3));
		problem.addConstraint(new Neq(v2, v3));

		final MAC solver = new MAC(problem);

		for (;;) {
			final SolverResult sol = solver.nextSolution();
			if (sol.isSat()) {
				System.out.println(sol);
				noGoodsConstraint.removeTuple(new int[] { (Integer) sol.get().apply("V0"),
						(Integer) sol.get().apply("V1") });
				solver.reset();
			} else {
				break;
			}
		}

	}
}
