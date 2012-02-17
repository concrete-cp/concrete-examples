package bigleq
import cspfj.problem.Problem
import cspfj.problem.BitVectorDomain
import cspfj.constraint.semantic.Gt
import cspfj.constraint.semantic.AllDifferent
import cspfj.StatisticsManager
import cspfj.MAC
import cspfj.ParameterManager
import cspfj.filter.AC3
import cspfj.Solver
import cspfj.filter.AC3Constraint
import cspfj.constraint.semantic.BoundAllDiff
import cspom.CSPOM

object BigLeq {
  val NB_VALS = 100;
  val NB_VARS = 100;

  def bigleq(nbVars: Int, nbVals: Int) = {
    val problem = new CSPOM();

    val vars = (0 until nbVars).map(i =>
      problem.interVar("X" + i, 0, nbVals - 1))

    for (v <- vars.sliding(2)) {
      problem.ctr("gt(" + v(1) + ", " + v(0) + ")");
    }

    problem.ctr("allDifferent" + vars.mkString("(", ", ", ")"))
    //problem.addConstraint(new BoundAllDiff(vars: _*));

    problem;
  }

  def main(args: Array[String]) {
    Solver.loggerLevel = "INFO"
    val problem = bigleq(NB_VARS, NB_VALS);
    //problem.variable("X0").dom.remove(0);

    ParameterManager("mac.filter") = classOf[AC3Constraint]

    println(problem.toXCSP)
    //val s = new MAC(problem);

    //val (result, time) = StatisticsManager.time(s.nextSolution)

    //println(time);

  }
}