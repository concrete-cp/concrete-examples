package golombruler

import cspom.CSPOM
import CSPOM._
import concrete.CSPOMDriver._
import concrete.Solver

object GolombRuler extends App {
  val TICKS = 8
  val MAX = TICKS * TICKS

  val problem = CSPOM {
    val variables = for (i <- 1 to TICKS) yield interVar(1, MAX) as s"T$i"

    for (
      xi <- variables; xj <- variables if xi != xj;
      xk <- variables; xl <- variables if xk != xl && (xi != xk || xj != xl)
    ) {
      ctr((xi - xj) !== (xk - xl))
    }
  }

  println(problem.constraints.size + " constraints")
  
  val solver = Solver(problem)

  println(solver.problem)

  solver.statistics.digest.foreach(println)

}