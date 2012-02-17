package xcsp;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cspfj.Pair;
import cspfj.ParameterManager;
import cspfj.Solver;
import cspfj.filter.AC3;
import cspfj.filter.Filter;
import cspfj.generator.FailedGenerationException;
import cspfj.heuristic.DDegOnDom;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.CSPOM;
import cspom.CSPParseException;
import cspom.compiler.ProblemCompiler;

public class XCSPSolver {

	public static void main(String[] args) throws CSPParseException,
			IOException, FailedGenerationException, InterruptedException {
		// System.out.println(Arrays.toString(args));
		final CSPOM cspomProblem = cspom.CSPOM.load(new URL(
				"file:///home/vion/CPAI08/" + args[0] + ".xml.bz2"));

		// ParameterManager.parameter("logger.level", "INFO");
		ParameterManager.update("heuristic.variable", DDegOnDom.class);

		final String[] parameters = args[1].split(":");

		ParameterManager.parse("mac.filter", parameters[0]);
		ParameterManager.parse("ac.queue", parameters[1]);

		ProblemCompiler.compile(cspomProblem);

		final Solver solver = Solver.factory(cspomProblem);
		// System.out.println(solver.problem());
		// System.out.println(solver.XMLConfig());
		final long time = System.currentTimeMillis();
		solver.nextSolution();
		System.out.print((System.currentTimeMillis() - time) / 1000.0);
		System.out.print(" ");
		System.out.println(solver.statistics().apply("solver.nbAssignments"));
	}

	public static boolean control(Problem problem) throws InterruptedException {
		final Filter ac = new AC3(problem);
		if (!ac.reduceAll()) {
			return false;
		}

		for (Variable vi : problem.getVariables()) {
			for (int a = vi.dom().first(); a >= 0; a = vi.dom().next(a)) {

				final Set<Pair> domain = new HashSet<Pair>();
				for (Variable vj : problem.getVariables()) {
					for (int b = vj.dom().first(); b >= 0; b = vj.dom().next(b)) {

						problem.push();
						vj.dom().setSingle(b);

						if (ac.reduceAfter(vj) && vi.dom().present(a)) {
							domain.add(new Pair(vj, b));
						}

						problem.pop();
					}
				}

				problem.push();
				for (Variable v : problem.getVariables()) {
					for (int i = v.dom().first(); i >= 0; i = v.dom().next(i)) {
						if (!domain.contains(new Pair(v, i))) {
							v.dom().remove(i);
						}
					}
					if (v.dom().size() == 0) {
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
