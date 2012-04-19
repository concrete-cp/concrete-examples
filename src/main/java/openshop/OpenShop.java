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

import scala.collection.Set;
import cspfj.Solver;
import cspfj.SolverResult;
import cspfj.generator.FailedGenerationException;
import cspom.CSPOM;
import cspom.compiler.ProblemCompiler;
import cspom.constraint.CSPOMConstraint;

public class OpenShop {
  public static void main(final String[] args) throws IOException,
      FailedGenerationException {
    Logger.getLogger("").setLevel(Level.INFO);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
    final Options opt = new Options();

    opt.addOption("lb", true, "Lower Bound");
    opt.addOption("ub", true, "Upper Bound");
    opt.addOption("js", false, "Job Shop");

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
      formatter.printHelp(
          "OpenShop { filename | size durationSeed machineSeed }",
          opt);
      System.exit(1);
      throw new InvalidParameterException();
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

    long totalTime = 0;

    while (ub > lb) {
      System.out.println("[" + lb + "," + ub + "]");
      final int test = (ub + lb) / 2;
      System.out.println("Test " + test);

      long time = -System.currentTimeMillis();

      generator.setUB(test);
      final CSPOM cspom = generator.generate();
      ProblemCompiler.compile(cspom);

      // System.out.println(cspom);

      final Solver solver = Solver.factory(cspom);

      final SolverResult solution = solver.nextSolution();
      time += System.currentTimeMillis();
      totalTime += time;
      System.out.print("In " + time / 1000f + ": ");
      if (!solution.isSat()) {
        System.out.println("UNSAT");

        lb = test + 1;
      } else {
        // generator.display(solution);
        final Set<CSPOMConstraint> control = cspom.control(solution
            .getNum());
        if (control.size() > 0) {
          throw new IllegalStateException(control.toString());
        }
        ub = generator.evaluate(solution.getInteger());
        System.out.println(ub);
      }

      System.out.println(solver.statistics().digest());
      System.out.println();

    }
    System.out.println(ub + "! (" + totalTime / 1000f + ")");
  }

}
