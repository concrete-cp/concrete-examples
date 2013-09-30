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

import java.io.IOException
import java.security.InvalidParameterException
import java.util.logging.Level
import java.util.logging.Logger
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import scala.collection.Set
import concrete.Solver
import concrete.SolverResult
import concrete.generator.FailedGenerationException
import cspom.CSPOM
import cspom.compiler.ProblemCompiler
import concrete.ParameterManager
import concrete.generator.cspompatterns.Patterns

object OpenShop extends App {
  ParameterManager("logger.level") = "INFO"

  val opt = new Options();

  opt.addOption("lb", true, "Lower Bound");
  opt.addOption("ub", true, "Upper Bound");
  opt.addOption("js", false, "Job Shop");

  val cl = try {
    new GnuParser().parse(opt, args);
  } catch {
    case e: ParseException =>
      val formatter = new HelpFormatter();
      formatter.printHelp("", opt);
      sys.exit(1);
  }

  val generator = cl.getArgs() match {
    case Array(arg) => OpenShopGenerator(arg);

    case Array(size, durSeed, macSeed) => OpenShopGenerator(size.toInt, durSeed.toInt, macSeed.toInt, cl.hasOption("js"));

    case _ =>
      val formatter = new HelpFormatter();
      formatter.printHelp("OpenShop { filename | size durationSeed machineSeed }", opt);
      sys.exit(1);
  }

  println(generator);

  var lb: Int =
    if (cl.hasOption("lb")) {
      cl.getOptionValue("lb").toInt
    } else {
      generator.getLB
    }

  var ub: Int =
    if (cl.hasOption("ub")) {
      cl.getOptionValue("ub").toInt
    } else {
      generator.ub
    }

  var totalTime = 0L;

  while (ub > lb) {
    println("[" + lb + "," + ub + "]");
    val test = (ub + lb) / 2;
    println("Test " + test);

    var time = -System.currentTimeMillis();

    generator.ub = test;
    val cspom = generator.generate();
    ProblemCompiler.compile(cspom, Patterns());

    // System.out.println(cspom);

    val solver = Solver(cspom);

    val solution = solver.toIterable.headOption
    time += System.currentTimeMillis();
    totalTime += time;
    print("In " + time / 1000f + ": ");

    solution match {
      case None =>
        println("UNSAT");
        lb = test + 1;
      case Some(sol) =>
        val control = cspom.controlInt(sol);
        if (control.nonEmpty) {
          throw new IllegalStateException(control.toString);
        }
        ub = generator.evaluate(sol);
        println(ub);
    }

    println(solver.statistics.digest);
    println();

  }
  System.out.println(ub + "! (" + totalTime / 1000f + ")");

}
