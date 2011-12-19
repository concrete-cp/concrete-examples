import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import scala.Option;
import scala.collection.immutable.Map;
import cspfj.MGACIter;
import cspfj.constraint.extension.ExtensionConstraintGeneral;
import cspfj.constraint.extension.TupleSet;
import cspfj.constraint.semantic.Neq;
import cspfj.problem.BitVectorDomain;
import cspfj.problem.Problem;
import cspfj.problem.Variable;

public class TestCspfj {
	public static void main(String[] args) throws IOException {
		Logger.getLogger("").setLevel(Level.WARNING);
		Problem problem = new Problem();

		cspfj.problem.Variable v0 = problem.addVariable("V0",
				new BitVectorDomain(new int[] { 0, 1, 2 }));
		cspfj.problem.Variable v1 = problem.addVariable("V1",
				new BitVectorDomain(new int[] { 0, 1, 2 }));
		cspfj.problem.Variable v2 = problem.addVariable("V2",
				new BitVectorDomain(new int[] { 0, 1, 2 }));
		cspfj.problem.Variable v3 = problem.addVariable("V3",
				new BitVectorDomain(new int[] { 0, 1, 2 }));

		final ExtensionConstraintGeneral noGoodsConstraint = new ExtensionConstraintGeneral(
				new TupleSet(true), false, new Variable[] { v0 });

		problem.addConstraint(noGoodsConstraint);

		problem.addConstraint(new Neq(v0, v2));
		problem.addConstraint(new Neq(v0, v1));
		problem.addConstraint(new Neq(v0, v3));
		problem.addConstraint(new Neq(v1, v3));
		problem.addConstraint(new Neq(v2, v3));

		MGACIter solver = new MGACIter(problem);

		for (;;) {
			final Option<Map<String, Object>> sol = solver.nextSolution();
			if (sol.isDefined()) {
				System.out.println(sol);
				noGoodsConstraint.removeTuple(new int[] { (Integer) sol.get()
						.apply("V0") });
				solver.reset();
			} else {
				break;
			}
		}

	}
}
