package bigleq

import cspfj.constraint.semantic.Gt
import cspfj.Problem
import cspom.CSPOM
import CSPOM._

object BigLeq extends App {
  val NB_VALS = 2000;
  val NB_VARS = 2000;

  def bigleq(nbVars: Int, nbVals: Int) = CSPOM {
    val vars = (1 to nbVars).map {
      case i if i == 2 => interVar("V" + i, 2, nbVals)
      case i => interVar("V" + i, 1, nbVals)
    }

    for (v <- vars.sliding(2)) {
      ctr("ge", v(1), v(0));
    }

    ctr("allDifferent", vars: _*)
  }

  //Solver.loggerLevel = "FINE"
  //val problem = bigleq(NB_VARS, NB_VALS);
  //problem.variable("X0").dom.remove(0);

  //ParameterManager("dummy.filter") = classOf[AC3]

  //for (i <- List(50, 100, 200, 500, 1000, 2000, 5000)) {

  val i = 400
  val problem = bigleq(i, i)
  xml.XML.save("bigleq-" + i + ".xml", problem.toXCSP)

  //}

  //val cspfj = ProblemGenerator.generate(problem)

  //println(cspfj)
  //val s = new DummySolver(cspfj);

  // val (result, time) = StatisticsManager.time(s.nextSolution)
  //println(cspfj)

  //println(result)
  //println(time);

}