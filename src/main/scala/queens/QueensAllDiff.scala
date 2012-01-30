package queens
import cspfj.problem.Problem
import cspfj.constraint.semantic.AllDifferent
import cspfj.problem.BitVectorDomain
import cspfj.constraint.semantic.Eq
import cspfj.Solver
import cspfj.StatisticsManager
import cspfj.ParameterManager
import cspfj.heuristic.Dom
import cspfj.constraint.semantic.AllDifferentAC
import cspfj.problem.Variable
import cspfj.constraint.semantic.Neq

object QueensAllDiff {
  def qp(size: Int) = {
    val problem = new Problem

    val queens = (0 until size) map (q => problem.addVariable("q" + q, new BitVectorDomain(0 until size: _*)))

    allDiff(problem, queens)

    val qd1 = queens.zipWithIndex map {
      case (q, i) =>
        val v = problem.addVariable("d1_" + q.name, new BitVectorDomain(0 - i until size - i: _*))
        problem.addConstraint(new Eq(1, q, -i, v))
        v
    }

    allDiff(problem, qd1)

    val qd2 = queens.zipWithIndex map {
      case (q, i) =>
        val v = problem.addVariable("d2_" + q.name, new BitVectorDomain(0 + i until size + i: _*))
        problem.addConstraint(new Eq(1, q, i, v))
        v
    }

    allDiff(problem, qd2)

    (queens, problem)
  }

  def allDiff(p: Problem, q: Seq[Variable]) {
    for (Seq(v1, v2) <- q.combinations(2)) {
      p.addConstraint(new Neq(v1, v2))
    }
  }

  def count(s: Solver) = {
    var i = 0
    while (s.nextSolution.isDefined)
      i += 1

    i
  }

  def main(args: Array[String]) {
    //ParameterManager.parameter("heuristic.variable", classOf[Dom])
    //ParameterManager.parameter("logger.level", "INFO")

    for (size <- List(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 20, 30, 50, 80, 100, 120, 150, 180, 200, 300, 400, 500, 800, 1000)) {
      print(size + ": ")
      val (queens, problem) = qp(size)

      val solver = Solver.factory(problem)

      val (s, time) = StatisticsManager.time(solver.nextSolution)

      println(time + ", " + s)
    }
  }
}