package queens

import cspfj.filter.ACC
import cspfj.heuristic.DDegOnDom
import cspfj.ParameterManager
import cspfj.Solver
import cspom.compiler.ProblemCompiler
import cspom.variable.CSPOMVariable
import cspom.CSPOM

object QueensAllDiffCSPOM extends App {
  def qp(size: Int) = {
    val problem = new CSPOM

    val queens = (0 until size) map (q => problem.interVar(0, size - 1))

    allDiff(problem, queens)

    val qd1 = queens.zipWithIndex map {
      case (q, i) =>
        val v = problem.interVar(0 - i, size - i - 1)
        problem.ctr("eq(%s, add(%s, %d))".format(v, q, -i))
        v
    }

    allDiff(problem, qd1)

    val qd2 = queens.zipWithIndex map {
      case (q, i) =>

        val v = problem.interVar(0 + i, size + i - 1)
        problem.ctr("eq(%s, add(%s, %d))".format(v, q, i))
        v
    }

    allDiff(problem, qd2)

    (queens, problem)
  }


  def allDiff(p: CSPOM, q: Seq[CSPOMVariable]) {

    p.ctr(q.mkString("alldifferent(", ", ", ")"))

  }

  def count(s: Solver) = {
    var i = 0
    while (s.nextSolution.isSat)
      i += 1

    i
  }

  def sol(s: Solver) = s.nextSolution

  ParameterManager("heuristic.variable") = classOf[cspfj.heuristic.DDegOnDom]

  //ParameterManager("logger.level") = "INFO"

  ParameterManager("mac.filter") = classOf[cspfj.filter.ACC]

  var sz = 400.0

  do {
    val size = sz.intValue
    print(size + " : ")
    val (queens, problem) = qp(size)
    ProblemCompiler.compile(problem)

    xml.XML.save("queensAllDiff-" + sz.toInt + ".xml", problem.toXCSP)
    //println(problem)

    //    val solver = Solver.factory(problem)
    //    //solver.maxBacktracks = -1
    //
    //    val (s, time) = StatisticsManager.time(sol(solver))
    //    //      for (v <- queens) {
    //    //        print(s.get(v.name) + " ")
    //    //      }
    //    //      println
    //    println("%g : %d".format(time, solver.statistics("solver.nbAssignments")))
    sz *= 1.1
  } while (false)

}