package pigeons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import concrete.JCSPOMDriver;
import concrete.Solver;
import concrete.generator.FailedGenerationException;
import cspom.CSPOM;
import cspom.variable.IntVariable;

public final class Pigeons {

	private Pigeons() {

	}

	public static CSPOM generate(int size) throws FailedGenerationException {
		final JCSPOMDriver p = new JCSPOMDriver();
		final List<IntVariable> variables = new ArrayList<IntVariable>(size);
		for (int i = size; --i >= 0;) {
			variables.add(IntVariable.ofInterval(1, size - 1));
		}

		for (int i = size; --i >= 0;) {
			for (int j = i; --j >= 0;) {
				p.ctr(p.neq(variables.get(i), variables.get(j)));
			}
		}
		return p;
	}

	public static void main(final String[] args) throws NumberFormatException,
			FailedGenerationException, IOException {

		for (int i : Arrays.asList(4, 8, 12, 15, 20, 30, 50, 80, 100, 120, 150)) {
			System.out.println(i + " :");
			long time = -System.currentTimeMillis();
			final CSPOM problem = generate(i);

			final Solver solver = Solver.apply(problem);

			solver.nextSolution();

			System.out.println((System.currentTimeMillis() + time) / 1000f);
			// while (solution != null) {
			// solution = solver.nextSolution();
			// count++;
			// }
			// System.out.println(count + " in "
			// + (System.currentTimeMillis() + time) / 1e3f);
		}
	}

}
