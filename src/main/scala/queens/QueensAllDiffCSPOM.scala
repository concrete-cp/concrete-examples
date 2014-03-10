package queens

import concrete.filter.ACC
import concrete.heuristic.DDegOnDom
import concrete.ParameterManager
import concrete.Solver
import cspom.compiler.ProblemCompiler
import cspom.variable.CSPOMVariable
import cspom.CSPOM
import CSPOM._
import cspom.StatisticsManager
import concrete.generator.ProblemGenerator
import concrete.CSPOMDriver._
import cspom.variable.IntVariable

object QueensAllDiffCSPOM extends App {
  def qp(n: Int) = CSPOM {

    val queens = 1 to n map (i => IntVariable(1 to n) as s"Q$i")

    ctr(allDifferent(queens: _*))

    val qd1 = queens.zipWithIndex map {
      case (q, i) => q - i
    }

    ctr(allDifferent(qd1: _*))

    val qd2 = queens.zipWithIndex map {
      case (q, i) => q + i
    }

    ctr(allDifferent(qd2: _*))
  }

  //ParameterManager("heuristic.variable") = classOf[concrete.heuristic.DDegOnDom]

  //ParameterManager("logger.level") = "INFO"

  //ParameterManager("mac.filter") = classOf[concrete.filter.ACC]

  for (size <- List(4, 8, 12, 20, 50, 100, 200, 500, 1000, 2000, 5000)) {
    //print(size + " : ")
    val problem = qp(size)

    //xml.XML.save("queensAllDiff-" + size + ".xml", problem.toXCSP)
    //println(problem)

    val solver = Solver(problem)
    //solver.maxBacktracks = -1

    val (s, time) = StatisticsManager.time(solver.hasNext)
    //      for (v <- queens) {
    //        print(s.get(v.name) + " ")
    //      }
    //      println
    println(f"$size : $time%f, ${solver.statistics("solver.nbAssignments")}")
  }

}