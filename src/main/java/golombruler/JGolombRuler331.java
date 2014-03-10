package golombruler;

import java.util.ArrayList;
import java.util.List;

import concrete.JCSPOMDriver;
import concrete.Solver;
import concrete.generator.cspompatterns.ConcretePatterns;
import cspom.CSPOM;
import cspom.variable.IntVariable;
import static concrete.JCSPOMDriver.*;
import static cspom.JCSPOM.*;

public class JGolombRuler331 {

	
	
	public static void main(String[] args) {
		ConcretePatterns.improveModel_$eq(Boolean.valueOf(args[1]));

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

		Solver solver = Solver.apply(p);
		solver.minimize("T" + ticks);
		//
		// println(solver.problem)

		while (solver.hasNext()) {
			System.out.println(solver.next());
		}

		System.out.println(solver.statistics()); // .digest.foreach(println)
	}
}