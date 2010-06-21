import cspfj.constraint.semantic.AllDifferent;
import cspfj.constraint.semantic.Gt;
import cspfj.filter.AC3;
import cspfj.filter.Filter;
import cspfj.problem.BitVectorDomain;
import cspfj.problem.Problem;
import cspfj.problem.Variable;

public class BigLeq {
	private static final int NB_VALS = 500;
	private static final int NB_VARS = 500;

	public static void main(String[] args) {

		final Problem problem = new Problem();
		final int[] vals = new int[NB_VALS];
		for (int i = NB_VALS; --i >= 0;) {
			vals[i] = i;
		}
		final Variable[] vars = new Variable[NB_VARS];
		for (int i = NB_VARS; --i >= 0;) {
			vars[i] = problem.addVariable("X" + i, new BitVectorDomain(vals));
		}
		problem.prepareVariables();
		for (int i = NB_VARS - 1; --i >= 0;) {
			problem.addConstraint(new Gt(vars[i + 1], vars[i], false));
		}

		problem.addConstraint(new AllDifferent(vars));
		problem.prepareConstraints();
		vars[0].remove(0);

		final Filter f = new AC3(problem);
		f.reduceAfter(vars[0]);

		System.out.println(f.getStatistics().get("revisions"));

	}
}
