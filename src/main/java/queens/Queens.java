package queens;

import java.io.IOException;
import java.util.Arrays;

import concrete.ParameterManager;
import concrete.Solver;
import concrete.generator.FailedGenerationException;

import cspom.CSPOM;
import cspom.compiler.ProblemCompiler;
import cspom.variable.CSPOMVariable;

public final class Queens {
	private final int size;
	private final CSPOMVariable[] variables;

	private Queens(final int size) {
		this.size = size;
		variables = new CSPOMVariable[size];
	}

	public CSPOM generate() {
		final CSPOM problem = new CSPOM();

		for (int i = size; --i >= 0;) {
			variables[i] = problem.interVar("Q" + i, 1, size);
		}

		for (int j = size; --j >= 0;) {
			for (int i = j; --i >= 0;) {
				problem.ctr(variables[i].ne(variables[j], problem));
				problem.ctr("ne", problem.is("abs", variables[i].$less(variables[j], problem)),
						problem.varOf(j - i));
			}
		}

		return problem;
	}

	public static void main(String[] args) throws FailedGenerationException, NumberFormatException,
			IOException, ClassNotFoundException {
		// ParameterManager.parse("logger.level", "INFO");
		ParameterManager.parse("heuristic.variable", "concrete.heuristic.WDegFixedOnDom");
		for (int i : Arrays.asList(4, 8, 12, 15, 20, 30, 50, 80, 100, 120, 150)) {
			System.out.println(i + " :");
			long time = -System.currentTimeMillis();
			final Queens queens = new Queens(i);
			final CSPOM problem = queens.generate();
			ProblemCompiler.compile(problem);

			final Solver solver = Solver.apply(problem);

			solver.nextSolution();

			System.out.println((System.currentTimeMillis() + time) / 1000f);
			System.out.println(solver.statistics().digest());
			// while (solution != null) {
			// solution = solver.nextSolution();
			// count++;
			// }
			// System.out.println(count + " in "
			// + (System.currentTimeMillis() + time) / 1e3f);
		}
	}
}
