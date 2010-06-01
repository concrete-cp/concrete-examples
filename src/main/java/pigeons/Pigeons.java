package pigeons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspom.CSPOM;
import cspom.DuplicateVariableException;
import cspom.compiler.PredicateParseException;
import cspom.compiler.ProblemCompiler;
import cspom.variable.CSPOMVariable;

public final class Pigeons {

	private Pigeons() {

	}

	public static CSPOM generate(int size) throws FailedGenerationException,
			DuplicateVariableException, PredicateParseException {
		final CSPOM problem = new CSPOM();
		final List<CSPOMVariable> variables = new ArrayList<CSPOMVariable>(size);
		for (int i = size; --i >= 0;) {
			variables.add(problem.var("V" + i, 0, size - 2));
		}

		for (int i = size; --i >= 0;) {
			for (int j = i; --j >= 0;) {
				problem.ctr("ne(" + variables.get(i) + ", " + variables.get(j)
						+ ")");
			}
		}
		return problem;
	}

	public static void main(final String[] args) throws NumberFormatException,
			FailedGenerationException, IOException, DuplicateVariableException,
			PredicateParseException {
		final CSPOM problem = generate(Integer.parseInt(args[0]));
		ProblemCompiler.compile(problem);
		final Solver solver = new MGACIter(ProblemGenerator.generate(problem));
		final Map<String, Integer> result = solver.nextSolution();
		System.out.println(result);

	}

}
