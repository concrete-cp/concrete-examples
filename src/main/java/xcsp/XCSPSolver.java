package xcsp;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import concrete.functionaltests.ConcreteTest;
import cspfj.MGACIter;
import cspfj.exception.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspfj.problem.Problem;
import cspom.CSPParseException;
import cspom.compiler.ProblemCompiler;
import cspom.constraint.CSPOMConstraint;

public class XCSPSolver {
	private static int count(final String name) throws CSPParseException,
			IOException, FailedGenerationException {
		final cspom.CSPOM cspomProblem = cspom.CSPOM.load(ConcreteTest.class
				.getResource(name));
		ProblemCompiler.compile(cspomProblem);
		final Problem problem = ProblemGenerator.generate(cspomProblem);
		// System.out.println(problem);

		final MGACIter solver = new MGACIter(problem);

		int count = 0;
		for (;; count++) {
			final Map<String, Integer> solution = solver.nextSolution();

			if (solution == null) {
				break;
			}
			final Map<String, Number> numSolution = new LinkedHashMap<String, Number>(
					solution.size());
			for (Entry<String, Integer> e : solution.entrySet()) {
				numSolution.put(e.getKey(), e.getValue());
			}
			final Collection<CSPOMConstraint> failed = cspomProblem
					.control(numSolution);
			if (!failed.isEmpty()) {
				throw new IllegalStateException(1 + count + "th solution: "
						+ failed.toString());
			}

		}

		return count;
	}

	public static void main(String[] args) throws CSPParseException, IOException, FailedGenerationException {
		System.out.println(count("frb35-17-1_ext.xml.bz2"));
	}
}
