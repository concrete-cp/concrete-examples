package bigleq

import cspfj.constraint.semantic.BoundAllDiff
import cspfj.constraint.semantic.Gt
import cspfj.filter.AC3Constraint
import cspfj.problem.Problem
import cspfj.DummySolver
import cspfj.ParameterManager
import cspfj.Solver
import cspfj.StatisticsManager
import cspom.CSPOM
import cspfj.generator.ProblemGenerator
import cspfj.filter.AC3
import cspfj.MAC
import java.io.File
import java.io.FileOutputStream

object BigLeq {
  val NB_VALS = 2000;
  val NB_VARS = 2000;

  def bigleq(nbVars: Int, nbVals: Int) = {
    val problem = new CSPOM();

    val vars = (1 to nbVars).map {
      case i if i == 2 => problem.interVar("V" + i, 2, nbVals)
      case i => problem.interVar("V" + i, 1, nbVals)
    }

    for (v <- vars.sliding(2)) {
      problem.ctr("ge(" + v(1) + ", " + v(0) + ")");
    }

    problem.ctr("allDifferent" + vars.mkString("(", ", ", ")"))
    //problem.addConstraint(new BoundAllDiff(vars: _*));

    problem;
  }

  def main(args: Array[String]) {
    //Solver.loggerLevel = "FINE"
    val problem = bigleq(NB_VARS, NB_VALS);
    //problem.variable("X0").dom.remove(0);

    ParameterManager("dummy.filter") = classOf[AC3]

    //for (i <- List(50, 100, 200, 500, 1000, 2000, 5000)) {

    // val problem = bigleq(i, i)
    // xml.XML.save("bigleq-" + i + ".xml", problem.toXCSP)

    //}

    val cspfj = ProblemGenerator.generate(problem)

    //println(cspfj)
    val s = new DummySolver(cspfj);

    val (result, time) = StatisticsManager.time(s.nextSolution)
    //println(cspfj)

    println(result)
    println(time);

  }
}