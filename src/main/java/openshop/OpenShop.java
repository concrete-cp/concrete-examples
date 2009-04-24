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
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cspfj.AbstractSolver;
import cspfj.MGACIter;
import cspfj.ResultHandler;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.CDC;
import cspfj.problem.Problem;
import cspfj.util.Chronometer;

public class OpenShop {
	public static void main(final String[] args) throws IOException,
			FailedGenerationException {
		Logger.getLogger("").setLevel(Level.INFO);
		Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
		final Options opt = new Options();

		opt.addOption("lb", true, "Lower Bound");
		opt.addOption("ub", true, "Upper Bound");
		opt.addOption("dc", false, "Dual Consistency");
		opt.addOption("cdc", false, "Conservative Dual Consistency");
		opt.addOption("js", false, "Job Shop");
		opt.addOption("f", true, "Factor");

		final CommandLine cl;
		try {
			cl = new GnuParser().parse(opt, args);
		} catch (ParseException e) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("", opt);
			System.exit(1);
			throw new InvalidParameterException();
		}

		final OpenShopGenerator generator;
		switch (cl.getArgs().length) {
		case 1:
			generator = new OpenShopGenerator(cl.getArgs()[0]);
			break;

		case 3:
			generator = new OpenShopGenerator(Integer.valueOf(cl.getArgs()[0]),
					Integer.valueOf(cl.getArgs()[1]), Integer.valueOf(cl
							.getArgs()[2]), cl.hasOption("js"));
			break;

		default:
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("", opt);
			System.exit(1);
			throw new InvalidParameterException();
		}

		if (cl.hasOption("f")) {
			generator.factor(Float.valueOf(cl.getOptionValue("f")));
		}

		System.out.println(generator);

		int lb;
		if (cl.hasOption("lb")) {
			lb = Integer.valueOf(cl.getOptionValue("lb"));
		} else {
			lb = generator.getLB();
		}
		int ub;
		if (cl.hasOption("ub")) {
			ub = Integer.valueOf(cl.getOptionValue("ub"));
		} else {
			ub = generator.getUB();
		}

		float time = 0;

		final ResultHandler rh = new ResultHandler(false);

		while (ub > lb) {
			System.out.println("[" + lb + "," + ub + "]");
			final int test = (ub + lb) / 2;
			// final int test = ub - 1;
			System.out.println("Test " + test);

			final Chronometer loadChrono = new Chronometer();
			loadChrono.startChrono();
			generator.setUB(test);
			final Problem problem = Problem.load(generator, -1);

			// generator.clear();

			final Solver solver = new MGACIter(problem, rh);

			if (cl.hasOption("cdc")) {
				solver.setUsePrepro(CDC.class);
			} else if (cl.hasOption("dc")) {
				solver.setUsePrepro(CDC.class);
				AbstractSolver.parameter("cdc.addConstraints", "true");
			}

			loadChrono.validateChrono();
			rh.load(solver, loadChrono.getUserTimeNano());

			// solver.setUseSpace(SPACE.CLASSIC);

			if (solver.runSolver()) {
				generator.display(solver.getSolution());

				ub = generator.evaluate(solver.getSolution());
			} else {
				System.out.println("UNSAT");

				lb = test + 1;
			}
			System.out.println();
			solver.collectStatistics();
			time += solver.getUserTime();
			System.out.println(lb + ", " + ub + " ("
					+ solver.getStatistics().get("prepro-cpu") + " + "
					+ solver.getStatistics().get("search-cpu") + "s, "
					+ solver.getNbAssignments() + " nodes)");

		}
		System.out.println(ub + "! (" + time + ")");
	}

}
