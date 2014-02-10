package queens;

import java.io.IOException;
import java.util.Arrays;

import concrete.JCSPOMDriver;
import concrete.ParameterManager;
import concrete.Solver;
import concrete.generator.FailedGenerationException;
import cspom.CSPOM;
import cspom.variable.IntVariable;
import static cspom.CSPOM.interVar;

public final class Queens {
	private final int size;
	private final IntVariable[] variables;

	private Queens(final int size) {
		this.size = size;
		variables = new IntVariable[size];
	}

	public CSPOM generate() {
		final JCSPOMDriver p = new JCSPOMDriver();

		for (int i = size; --i >= 0;) {
			variables[i] = interVar("Q" + i, 1, size);
		}

		for (int j = size; --j >= 0;) {
			for (int i = j; --i >= 0;) {
				p.ctr(p.ne(variables[i], variables[j]));
				p.ctr(p.ne(p.abs(p.less(variables[i], variables[j])), p.constant(j - i)));
			}
		}

		return p;
	}

	public static void main(String[] args) throws FailedGenerationException, NumberFormatException,
			IOException, ClassNotFoundException {
		// ParameterManager.parse("logger.level", "INFO");
		ParameterManager.update("heuristic.variable", concrete.heuristic.WDegOnDom.class);
		for (int i : Arrays.asList(80, 4, 8, 12, 15, 20, 30, 50, 80, 100, 120, 150)) {
			System.out.println(i + " :");
			long time = -System.currentTimeMillis();
			final Queens queens = new Queens(i);
			final CSPOM problem = queens.generate();

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
