package queens

import concrete.IntDomain
import concrete.Problem
import concrete.Solver
import concrete.Variable
import concrete.constraint.linear.Eq
import concrete.constraint.semantic.AllDifferent2C
import concrete.constraint.semantic.AllDifferentBC
import cspom.StatisticsManager
import concrete.ParameterManager

object QueensAllDiffCSPFJ {
  def qp(size: Int) = {

    val queens = IndexedSeq.tabulate(size)(q =>
      new Variable("q" + q, IntDomain(0 until size)))

    val qd1 = queens.zipWithIndex.map {
      case (q, i) => new Variable("d1_" + q.name, IntDomain(-i until size - i))
    }

    val qd2 = queens.zipWithIndex.map {
      case (q, i) => new Variable("d2_" + q.name, IntDomain(i until size + i))
    }

    val problem = Problem(queens ++ qd1 ++ qd2: _*)

    for (((q, q1, q2), i) <- (queens, qd1, qd2).zipped.toIterable.zipWithIndex) {
      Eq(false, q, -i, q1).foreach(problem.addConstraint)
      Eq(false, q, i, q2).foreach(problem.addConstraint)
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
    var sz = 10

    do {
      val size = sz.intValue
      print(size + " : ")
      val (queens, problem) = qp(size)

      val pm = new ParameterManager

      //pm("mac.restartLevel") = -1

      val solver = Solver(problem, pm)
      //solver.maxBacktracks = -1

      val stats = new StatisticsManager
      stats.register("solver", solver)
      val (ts, time) = StatisticsManager.measure(solver.next)

      val s = ts.get
      //println(queens.map(s).mkString(" "))
      //      println
      println(s"$time : ${stats("solver.nbAssignments")}")
      //      var count = 1
      //      while (solver.hasNext) {
      //        count += 1
      //        solver.next()
      //      }
      //      println(solver.statistics("solver.searchCpu"))
      //      println(count)
      sz += 10
    } while (true)
  }
}