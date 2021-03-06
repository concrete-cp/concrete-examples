package queens;

import static cspom.JCSPOM.constant;
import static cspom.JCSPOM.intVarRange;

import java.io.IOException;

import concrete.CSPOMSolver;
import concrete.JCSPOMDriver;
import concrete.ParameterManager;
import concrete.Solver;
import concrete.generator.FailedGenerationException;
import cspom.CSPOM;
import cspom.variable.CSPOMSeq;
import cspom.variable.IntVariable;

public final class Queens {
	private final int size;

	private Queens(final int size) {
		this.size = size;
	}

	public CSPOM generate() {
		final JCSPOMDriver p = new JCSPOMDriver();

		final IntVariable[] variables = new IntVariable[size];

		for (int i = size; --i >= 0;) {
			variables[i] = intVarRange(1, size);
		}

		p.nameExpression(p.seq(variables), "vars");

		for (int j = size; --j >= 0;) {
			for (int i = j; --i >= 0;) {
				p.ctr(p.neq(variables[i], variables[j]));
				p.ctr(p.neq(p.abs(p.less(variables[i], variables[j])),
						constant(j - i)));
			}
		}

		return p;
	}

	public static void main(String[] args) throws FailedGenerationException,
			NumberFormatException, IOException, ClassNotFoundException {
		// ParameterManager.parse("logger.level", "INFO");
		ParameterManager pm = new ParameterManager();
		pm.update("heuristic.variable", concrete.heuristic.LexVar.class);

		Queens q = new Queens(4);
		CSPOM problem = q.generate();
		CSPOMSolver solver = Solver.apply(problem, pm).get();

		if (solver.hasNext())
			System.out.println(solver.next());
		else
			System.out.println("UNSAT");

		// for (int i : Arrays.asList(8, 12, 15, 20, 30, 50, 80, 100, 120, 150))
		// {
		// System.out.println(i + " :");
		//
		// final Queens queens = new Queens(i);
		// final CSPOM problem = queens.generate();
		//
		// final CSPOMSolver solver = Solver.apply(problem, pm);
		//
		// solver.next();
		//
		// System.out.println(solver.statistics().apply("solver.searchCpu"));
		// System.out.println(solver.statistics().digest());
		// int count = 1;
		// while (solver.hasNext()) {
		// solver.next();
		// count++;
		// }
		// System.out.println(count);
		// System.out.println(solver.statistics().apply("solver.searchCpu"));
		// }
	}
}
