package queens

import cspfj.filter.ACC
import cspfj.heuristic.DDegOnDom
import cspfj.ParameterManager
import cspfj.Solver
import cspom.compiler.ProblemCompiler
import cspom.variable.CSPOMVariable
import cspom.CSPOM
import CSPOM._
import cspfj.StatisticsManager
import cspfj.generator.ProblemGenerator

object QueensAllDiffCSPOM extends App {
  def qp(size: Int) = {
    var queens: Seq[CSPOMVariable] = null

    val problem = CSPOM {

      queens = (0 until size) map (q => interVar(0, size - 1))

      allDiff(queens)

      val qd1 = queens.zipWithIndex map {
        case (q, i) =>
          val v = interVar(0 - i, size - i - 1)
          ctr(v == (q - i))
          v
      }

      allDiff(qd1)

      val qd2 = queens.zipWithIndex map {
        case (q, i) =>

          val v = interVar(0 + i, size + i - 1)
          ctr(v == (q + i))
          v
      }

      allDiff(qd2)
    }
    (queens, problem)
  }

  def allDiff(q: Seq[CSPOMVariable])(implicit p: CSPOM) {

    ctr('alldifferent(q: _*))

  }

  def count(s: Solver) = {
    var i = 0
    while (s.nextSolution.isSat)
      i += 1

    i
  }

  def sol(s: Solver) = s.nextSolution

  //ParameterManager("heuristic.variable") = classOf[cspfj.heuristic.DDegOnDom]

  ParameterManager("logger.level") = "INFO"

  //ParameterManager("mac.filter") = classOf[cspfj.filter.ACC]

  for (size <- List(4, 8, 12, 20, 50, 100, 200, 500, 1000, 2000, 5000)) {
    //print(size + " : ")
    val (queens, problem) = qp(size)
    ProblemCompiler.compile(problem)

    //xml.XML.save("queensAllDiff-" + size + ".xml", problem.toXCSP)
    //println(problem)

    val solver = Solver(problem)
    //solver.maxBacktracks = -1

    val (s, time) = StatisticsManager.time(sol(solver))
    //      for (v <- queens) {
    //        print(s.get(v.name) + " ")
    //      }
    //      println
    println(f"$size : $time%f, ${solver.statistics("solver.nbAssignments")}")
  }

}