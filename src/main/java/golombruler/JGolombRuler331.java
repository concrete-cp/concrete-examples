package golombruler;

import static cspom.JCSPOM.intVarRange;

import java.util.ArrayList;
import java.util.List;

import concrete.CSPOMSolver;
import concrete.JCSPOMDriver;
import concrete.ParameterManager;
import concrete.Solver;
import cspom.variable.IntVariable;

public class JGolombRuler331 {

	public static void main(String[] args) {
		ParameterManager pm = new ParameterManager();
		pm.update("improveModel", Boolean.valueOf(args[1]));

		final int ticks = Integer.valueOf(Integer.valueOf(args[0]));
		final int max = ticks * ticks;

		final JCSPOMDriver p = new JCSPOMDriver();

		final List<IntVariable> variables = new ArrayList<>();
		for (int i = 1; i <= ticks; i++) {

			variables.add(p.nameExpression(intVarRange(1, max), "T" + i));
		}

		for (int i = 0; i < ticks - 1; i++) {
			p.ctr(p.lt(variables.get(i), variables.get(i + 1)));
		}

		for (IntVariable xi : variables) {
			for (IntVariable xj : variables) {
				if (xi != xj) {
					for (IntVariable xk : variables) {
						for (IntVariable xl : variables) {
							if (xk != xl && (xi != xk || xj != xl)) {
								p.ctr(p.neq(p.less(xi, xj), p.less(xk, xl)));
							}
						}
					}
				}
			}
		}

		System.out.println(p.constraints().size() + " constraints");

		// println(problem)

		// println(statistics)

		CSPOMSolver solver = Solver.apply(p, pm).get();

		solver.minimize("T" + ticks);
		//
		// println(solver.problem)

		while (solver.hasNext()) {
			System.out.println(solver.next());
		}

		System.out.println(solver.statistics()); // .digest.foreach(println)
	}
}
