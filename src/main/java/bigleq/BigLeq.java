package bigleq;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.constraint.Constraint;
import cspfj.constraint.semantic.AllDifferent;
import cspfj.constraint.semantic.Gt;
import cspfj.filter.AC3Constraint;
import cspfj.priorityqueues.FibonacciHeap;
import cspfj.priorityqueues.Key;
import cspfj.problem.BitVectorDomain;
import cspfj.problem.Problem;
import cspfj.problem.Variable;

public class BigLeq {
	private static final int NB_VALS = 1000;
	private static final int NB_VARS = 1000;

	public static Problem bigleq(final int nbVars, final int nbVals) {
		final Problem problem = new Problem();

		final int[] vals = new int[nbVals];
		for (int i = nbVals; --i >= 0;) {
			vals[i] = i;
		}
		final Variable[] vars = new Variable[nbVars];
		for (int i = nbVars; --i >= 0;) {
			vars[i] = problem.addVariable("X" + i, new BitVectorDomain(vals));
		}

		for (int i = nbVars - 1; --i >= 0;) {
			problem.addConstraint(new Gt(vars[i + 1], vars[i], false));
		}

		problem.addConstraint(new AllDifferent(vars));

		return problem;
	}

	public static void main(String[] args) {

		final Problem problem = bigleq(NB_VARS, NB_VALS);
		problem.getVariable("X0").remove(0);

		{
			final Solver s = new MGACIter(problem);
			long time = -System.currentTimeMillis();
			s.nextSolution();
			time += System.currentTimeMillis();

			System.out.println(time);
		}

	}
}
