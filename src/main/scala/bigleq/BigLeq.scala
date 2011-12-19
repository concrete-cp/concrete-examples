package bigleq
import cspfj.problem.Problem
import cspfj.problem.BitVectorDomain
import cspfj.constraint.semantic.Gt
import cspfj.constraint.semantic.AllDifferent
import cspfj.StatisticsManager
import cspfj.MAC
import cspfj.ParameterManager
import cspfj.filter.AC3

object BigLeq {
  val NB_VALS = 1000;
  val NB_VARS = 1000;

  def bigleq(nbVars: Int, nbVals: Int) = {
    val problem = new Problem();

    val vars = (0 until nbVars).map(i =>
      problem.addVariable("X" + i, new BitVectorDomain(0 until nbVals: _*)))

    for (v <- vars.sliding(2)) {
      problem.addConstraint(new Gt(v(1), v(0), false));
    }

    problem.addConstraint(new AllDifferent(vars: _*));

    problem;
  }

  def main(args: Array[String]) {

    val problem = bigleq(NB_VARS, NB_VALS);
    problem.variable("X0").dom.remove(0);

    
    ParameterManager.parameter("mac.filter", classOf[AC3])
    
    val s = new MAC(problem);

    val (result, time) = StatisticsManager.time(s.nextSolution)

    System.out.println(time);

  }
}