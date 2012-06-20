package queens
import cspfj.Problem
import cspfj.constraint.semantic.Eq
import cspfj.Solver
import cspfj.StatisticsManager
import cspfj.ParameterManager
import cspfj.heuristic.Dom
import cspfj.Variable
import cspfj.constraint.semantic.Neq
import cspfj.heuristic.LexVar
import cspfj.constraint.semantic.AllDifferentBC
import cspfj.constraint.semantic.AllDifferent2C
import cspfj.IntDomain
import cspfj.constraint.Constraint

object QueensAllDiffCSPFJ {
  def qp(size: Int) = {
    val problem = new Problem

    val queens = (0 until size) map (q => problem.addVariable("q" + q, IntDomain(0 until size)))

    allDiff(problem, queens)

    val qd1 = queens.zipWithIndex map {
      case (q, i) =>
        val v = problem.addVariable("d1_" + q.name, IntDomain(0 - i until size - i))
        problem.addConstraint(new Eq(false, q, -i, v))
        v
    }

    allDiff(problem, qd1)

    val qd2 = queens.zipWithIndex map {
      case (q, i) =>
        val v = problem.addVariable("d2_" + q.name, IntDomain(0 + i until size + i))
        problem.addConstraint(new Eq(false, q, i, v))
        v
    }

    allDiff(problem, qd2)

    (queens, problem)
  }

  //      def allDiff(p: Problem, q: Seq[Variable]) {
  //        for (Seq(v1, v2) <- q.combinations(2)) {
  //          p.addConstraint(new Neq(v1, v2))
  //        }
  //      }

  def allDiff(p: Problem, q: Seq[Variable]) {
    p.addConstraint(new AllDifferent2C(q: _*))
    p.addConstraint(new AllDifferentBC(q: _*))

  }

  def count(s: Solver) = {
    var i = 0
    while (s.nextSolution.isSat)
      i += 1

    i
  }

  def sol(s: Solver) = s.nextSolution

  def main(args: Array[String]) {
    ParameterManager("heuristic.variable") = classOf[cspfj.heuristic.DDegOnDom]

    //ParameterManager("logger.level") = "INFO"

    //ParameterManager("mac.restartLevel") = -1
    ParameterManager("mac.filter") = classOf[cspfj.filter.ACV]
    ParameterManager("ac3c.queue") = classOf[cspfj.priorityqueues.JavaFifos[Constraint]]


    var sz = 210

    do {
      val size = sz.intValue
      print(size + " : ")
      val (queens, problem) = qp(size)

      val solver = Solver.factory(problem)
      //solver.maxBacktracks = -1

      val (s, time) = StatisticsManager.time(sol(solver))
      //      for (v <- queens) {
      //        print(s.get(v.name) + " ")
      //      }
      //      println
      println("%g : %d".format(time, solver.statistics("solver.nbAssignments")))
      sz += 1
    } while (true)
  }
}