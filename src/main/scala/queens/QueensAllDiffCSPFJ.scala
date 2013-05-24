package queens
import concrete.Problem
import concrete.constraint.semantic.Eq
import concrete.Solver
import concrete.StatisticsManager
import concrete.ParameterManager
import concrete.heuristic.Dom
import concrete.Variable
import concrete.constraint.semantic.Neq
import concrete.heuristic.LexVar
import concrete.constraint.semantic.AllDifferentBC
import concrete.constraint.semantic.AllDifferent2C
import concrete.IntDomain
import concrete.constraint.Constraint

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

  def main(args: Array[String]) {
    ParameterManager("heuristic.variable") = classOf[concrete.heuristic.DDegOnDom]
    ParameterManager("mac.filter") = classOf[concrete.filter.ACV]
    ParameterManager("ac3c.queue") = classOf[concrete.priorityqueues.QuickFifos[Variable]]

    var sz = 800

    do {
      val size = sz.intValue
      print(size + " : ")
      val (queens, problem) = qp(size)

      val solver = Solver(problem)
      //solver.maxBacktracks = -1

      val (s, time) = StatisticsManager.time(solver.hasNext)
      //      for (v <- queens) {
      //        print(s.get(v.name) + " ")
      //      }
      //      println
      println("%g : %d".format(time, solver.statistics("solver.nbAssignments")))
      sz = (sz * 1.1).toInt
    } while (true)
  }
}