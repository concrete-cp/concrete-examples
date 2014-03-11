package bigleq

import concrete.constraint.semantic.Gt
import concrete.Problem
import cspom.CSPOM
import CSPOM._
import concrete.CSPOMDriver._
import cspom.variable.IntVariable

object BigLeq extends App {
  val NB_VALS = 2000;
  val NB_VARS = 2000;

  def bigleq(nbVars: Int, nbVals: Int) = CSPOM {
    val vars = (1 to nbVars).map {
      case i if i == 2 => IntVariable(2 to nbVals) as ("V" + i)
      case i => IntVariable(1 to nbVals) as ("V" + i)
    }

    for (v <- vars.sliding(2)) {
      ctr(v(1) > v(0))
    }

    ctr(allDifferent(vars: _*))

  }

  //Solver.loggerLevel = "FINE"
  //val problem = bigleq(NB_VARS, NB_VALS);
  //problem.variable("X0").dom.remove(0);

  //ParameterManager("dummy.filter") = classOf[AC3]

  //for (i <- List(50, 100, 200, 500, 1000, 2000, 5000)) {

  val i = 400
  val problem = bigleq(i, i)
  //xml.XML.save("bigleq-" + i + ".xml", XCSPWriter(problem))

  //}

  //val concrete = ProblemGenerator.generate(problem)

  //println(concrete)
  //val s = new DummySolver(concrete);

  // val (result, time) = StatisticsManager.time(s.nextSolution)
  //println(concrete)

  //println(result)
  //println(time);

}
