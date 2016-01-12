package queens

import concrete.filter.ACC
import concrete.heuristic.DDegOnDom
import concrete.ParameterManager
import concrete.Solver
import cspom.compiler.CSPOMCompiler
import cspom.variable.CSPOMVariable
import cspom.CSPOM
import CSPOM._
import cspom.StatisticsManager
import concrete.generator.ProblemGenerator
import concrete.CSPOMDriver._
import cspom.variable.IntVariable
import cspom.CSPOMConstraint
import cspom.variable.BoolVariable
import java.util.Arrays
import cspom.variable.SimpleExpression
import concrete.CSPOMSolution

object MQueens extends App {
  def qp(n: Int) = CSPOM { implicit problem =>

    val queens = (0 until n) map { i => IntVariable(0 to n) as s"Q$i" }

    allDifferentBut0(queens: _*).foreach(ctr)

    val qd1 = queens.zipWithIndex map {
      case (q, i) => q - i
    }

    allDifferentBut0(qd1: _*).foreach(ctr)

    val qd2 = queens.zipWithIndex map {
      case (q, i) => q + i
    }

    allDifferentBut0(qd2: _*).foreach(ctr)

    for (i <- 0 until n; j <- 1 to n) {
      ctr((queens(i) !== 0) |
        (occurrence(j)(queens: _*) > 0) |
        (occurrence(j - i + 1)(qd1: _*) > 0) |
        (occurrence(j + i + 1)(qd2: _*) > 0))
    }

    val occurrences = occurrence(0)(queens: _*) as "occurrences"
  }

  def allDifferentBut0(q: SimpleExpression[Int]*)(implicit problem: CSPOM) = {
    for (Seq(q1, q2) <- q.combinations(2)) yield {
      (q1 === 0) | (q2 === 0) | (q1 !== q2)
    }
  }

  //ParameterManager("heuristic.variable") = classOf[concrete.heuristic.DDegOnDom]

  //ParameterManager("logger.level") = "INFO"

  //ParameterManager("mac.filter") = classOf[concrete.filter.ACC]

  for (size <- List(4, 8, 12, 20, 50, 100, 200, 500, 1000, 2000, 5000)) {
    //print(size + " : ")
    val problem = qp(size)

    val solver = Solver(problem).get
    solver.maximize("occurrences")
    //solver.maxBacktracks = -1

    val (res, time) = StatisticsManager.measure(solver.toIterable.last)

    val sol = res.get

    for { s: CSPOMSolution <- res } {
      println((0 until size).map(i => s(s"Q$i")))
    }
    //      for (v <- queens) {
    //        print(s.get(v.name) + " ")
    //      }
    //      println
    println(f"$size : ${time.value}%f, ${solver.statistics("solver.nbAssignments")}")
  }

}
