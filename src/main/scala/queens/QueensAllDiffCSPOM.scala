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

object QueensAllDiffCSPOM extends App {
  def qp(n: Int) = CSPOM withResult {

    val queens = Seq.fill(n)(interVar(1, n))

    ctr('alldifferent(queens: _*))

    val qd1 = queens.zipWithIndex map {
      case (q, i) => q - i
    }

    ctr('alldifferent(qd1: _*))

    val qd2 = queens.zipWithIndex map {
      case (q, i) => q + i
    }

    ctr('alldifferent(qd2: _*))

    queens
  }


  //ParameterManager("heuristic.variable") = classOf[concrete.heuristic.DDegOnDom]

  //ParameterManager("logger.level") = "INFO"

  //ParameterManager("mac.filter") = classOf[concrete.filter.ACC]

  for (size <- List(4, 8, 12, 20, 50, 100, 200, 500, 1000, 2000, 5000)) {
    //print(size + " : ")
    val (problem, queens) = qp(size)
    ProblemCompiler.compile(problem)

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