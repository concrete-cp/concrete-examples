/**
 * CSPFJ Competitor - CSP solver using the CSPFJ API for Java
 * Copyright (C) 2006 Julien VION
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package openshop;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import cspfj.MGACIter;
import cspfj.ResultHandler;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;
import cspfj.util.CpuMonitor;

public class OpenShop {
	public static void main(final String[] args) throws IOException {
		Logger.getLogger("").setLevel(Level.WARNING);
		final OpenShopGenerator generator = new OpenShopGenerator(args[0]);

		int lb = generator.getLB();
		int ub = generator.getUB();

		float time = 0;

		final ResultHandler rh = new ResultHandler(false);

		while (ub > lb) {
			System.out.println("[" + lb + "," + ub + "]");
			final int test = (ub + lb) / 2;
			// final int test = ub - 1;
			System.out.println("Test " + test);

			long load = -CpuMonitor.getCpuTimeNano();
			generator.setUB(test);
			Problem problem = null;
			try {
				problem = Problem.load(generator, 0);
			} catch (FailedGenerationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// generator.clear();

			final Solver solver = new MGACIter(problem, rh);
			load += CpuMonitor.getCpuTimeNano();
			rh.load(solver, load);

			// solver.setUseSpace(SPACE.CLASSIC);

			if (solver.runSolver()) {
				generator.display(solver.getSolution());

				ub = generator.evaluate(solver.getSolution());
			} else {
				System.out.println("UNSAT");
				
				lb = test + 1;
			}
			System.out.println();
			time += solver.getUserTime();
			// logger.warning(lb + ", " + ub + " (" + solver.getUserTime() +
			// "s)");

		}
		System.out.println(ub + "! (" + time + ")");
	}

}
