package xcsp;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.AC3;
import cspfj.filter.DC1;
import cspfj.filter.Filter;
import cspfj.generator.ProblemGenerator;
import cspfj.heuristic.Pair;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.CSPOM;
import cspom.CSPParseException;

public class XCSPSolver {

	public static void main(String[] args) throws CSPParseException,
			IOException, FailedGenerationException, InterruptedException {

		final CSPOM cspomProblem = cspom.CSPOM.load(XCSPSolver.class
				.getResource("bqwh-15-106-0_ext.xml"));

		final Solver solver = new MGACIter(cspomProblem);
		System.out.println(solver.nextSolution());
	}

	public static boolean control(Problem problem) throws InterruptedException {
		final Filter ac = new AC3(problem);
		if (!ac.reduceAll()) {
			return false;
		}

		for (Variable vi : problem.getVariables()) {
			for (int a = vi.getFirst(); a >= 0; a = vi.getNext(a)) {

				final Set<Pair> domain = new HashSet<Pair>();
				for (Variable vj : problem.getVariables()) {
					for (int b = vj.getFirst(); b >= 0; b = vj.getNext(b)) {

						problem.push();
						vj.setSingle(b);

						if (ac.reduceAfter(vj) && vi.isPresent(a)) {
							domain.add(new Pair(vj, b));
						}

						problem.pop();
					}
				}

				problem.push();
				for (Variable v : problem.getVariables()) {
					for (int i = v.getFirst(); i >= 0; i = v.getNext(i)) {
						if (!domain.contains(new Pair(v, i))) {
							v.remove(i);
						}
					}
					if (v.getDomainSize() == 0) {
						return false;
					}
				}
				if (!ac.reduceAll()) {
					return false;
				}
				problem.pop();
			}
		}

		return true;
	}

}
