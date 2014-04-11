package queens
import concrete.Problem
import concrete.constraint.semantic.Eq
import concrete.Solver
import cspom.StatisticsManager
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

    val queens = (0 until size) map (q => new Variable("q" + q, IntDomain(0 until size))) toList

    val qd1 = queens.zipWithIndex map {
      case (q, i) => new Variable("d1_" + q.name, IntDomain(-i until size - i))
    }

    val qd2 = queens.zipWithIndex map {
      case (q, i) => new Variable("d2_" + q.name, IntDomain(i until size + i))
    }

    val problem = new Problem(queens ::: qd1 ::: qd2)

    for (((q, q1, q2), i) <- (queens, qd1, qd2).zipped.toIterable.zipWithIndex) {
      problem.addConstraint(new Eq(false, q, -i, q1))
      problem.addConstraint(new Eq(false, q, i, q2))
    }

    allDiff(problem, queens)
    allDiff(problem, qd1)
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
    var sz = 12

    do {
      val size = sz.intValue
      print(size + " : ")
      val (queens, problem) = qp(size)

      val solver = Solver(problem)
      //solver.maxBacktracks = -1

      val (s, time) = StatisticsManager.time(solver.next)
      //      for (v <- queens) {
      //        print(s.get(v.name) + " ")
      //      }
      //      println
      println("%g : %d".format(time, solver.statistics("solver.nbAssignments")))
      var count = 1
      while (solver.hasNext) {
        count += 1
        solver.next()
      }
      println(solver.statistics("solver.searchCpu"))
      println(count)
      //sz = (sz * 1.1).toInt
    } while (true)
  }
}