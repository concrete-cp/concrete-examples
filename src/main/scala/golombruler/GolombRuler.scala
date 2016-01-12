package golombruler

import concrete.CSPOMDriver._
import concrete.Solver
import concrete.generator.ProblemGenerator
import concrete.generator.cspompatterns.AllDiff
import cspom.CSPOM
import cspom.CSPOM._
import cspom.StatisticsManager
import cspom.compiler.CSPOMCompiler
import cspom.compiler.StandardCompilers
import cspom.compiler.MergeEq
import concrete.generator.cspompatterns.ConcretePatterns
import cspom.variable.IntVariable
import cspom.variable.CSPOMVariable
import concrete.ParameterManager

object GolombRuler extends App {
  val pm = new ParameterManager()
  pm("improveModel") = args(1).toBoolean

  val ticks = args(0).toInt
  val max = ticks * ticks

  val problem = CSPOM { implicit problem =>
    val variables = for (i <- 1 to ticks) yield IntVariable(1 to max) as s"T$i"

    for (Seq(xi, xj) <- variables.sliding(2)) {
      ctr(xi < xj)
    }

    for (
      xi <- variables; xj <- variables if xi != xj;
      xk <- variables; xl <- variables if xk != xl &&
        (xi != xk || xj != xl)
    ) {
      ctr(xi - xj !== xk - xl)
    }
  }

  println(problem.constraints.size + " constraints")

  //println(problem)

  //println(statistics)

  val solver = Solver(problem, pm).get

  solver.minimize(s"T$ticks")
  //
  //  println(solver.problem)

  for (sol <- solver) {
    println((1 to ticks).map(i => sol(s"T$i")))
  }

  println(solver.statistics) //.digest.foreach(println)

}
