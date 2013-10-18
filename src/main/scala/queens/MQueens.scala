package queens

import concrete.filter.ACC
import concrete.heuristic.DDegOnDom
import concrete.ParameterManager
import concrete.Solver
import cspom.compiler.ProblemCompiler
import cspom.variable.CSPOMVariable
import cspom.CSPOM
import CSPOM._
import concrete.StatisticsManager
import concrete.generator.ProblemGenerator
import concrete.generator.cspompatterns.Patterns
import concrete.CSPOMDriver._
import cspom.variable.IntVariable
import cspom.CSPOMConstraint
import cspom.variable.BoolVariable

object MQueens extends App {
  def qp(n: Int) = CSPOM {

    val queens = (0 until n) map { i => interVar(s"Q$i", 0, n) }

    allDifferentBut0(queens: _*).foreach(ctr)

    val qd1 = queens.zipWithIndex map {
      case (q, i) => q - i
    }

    allDifferentBut0(qd1: _*).foreach(ctr)

    val qd2 = queens.zipWithIndex map {
      case (q, i) => q + i
    }

    allDifferentBut0(qd2: _*).foreach(ctr)

  }

  def allDifferentBut0(q: IntVariable*) = {
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
    ProblemCompiler.compile(problem, Patterns())

    //xml.XML.save("queensAllDiff-" + size + ".xml", problem.toXCSP)
    println(problem)

    val solver = Solver(problem)
    //solver.maxBacktracks = -1

    val (s, time) = StatisticsManager.time(solver.hasNext)

    val sol = solver.next()

    println((0 until size).map(i => sol(s"Q$i")))
    //      for (v <- queens) {
    //        print(s.get(v.name) + " ")
    //      }
    //      println
    println(f"$size : $time%f, ${solver.statistics("solver.nbAssignments")}")
  }

}